// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.templateengine.handlebars;

import static com.microsoft.semantickernel.semanticfunctions.KernelArguments.MAIN_KEY;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.ValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableType;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplate;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateOption;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.text.StringEscapeUtils;
import reactor.core.publisher.Mono;

/**
 * A prompt template that uses the Handlebars template engine to render prompts.
 */
public class HandlebarsPromptTemplate implements PromptTemplate {

    private final PromptTemplateConfig promptTemplate;

    /**
     * Initializes a new instance of the {@link HandlebarsPromptTemplate} class.
     *
     * @param promptTemplate The prompt template configuration.
     */
    public HandlebarsPromptTemplate(
        @Nonnull PromptTemplateConfig promptTemplate) {
        this.promptTemplate = new PromptTemplateConfig(promptTemplate);
    }

    @Override
    public Mono<String> renderAsync(
        Kernel kernel,
        @Nullable KernelArguments arguments,
        @Nullable InvocationContext context) {
        String template = promptTemplate.getTemplate();
        if (template == null) {
            return Mono.error(new SKException(
                String.format("No prompt template was provided for the prompt %s.",
                    promptTemplate.getName())));
        }

        if (context == null) {
            context = InvocationContext.builder().build();
        }
        HandleBarsPromptTemplateHandler handler = new HandleBarsPromptTemplateHandler(kernel,
            template, context);

        if (arguments == null) {
            arguments = KernelArguments.builder().build();
        }
        return handler.render(arguments);
    }

    private static class MessageResolver implements ValueResolver {

        @Override
        @SuppressWarnings("NullAway")
        public Object resolve(Object context, String name) {
            if (context instanceof ChatMessageContent) {
                if ("role".equalsIgnoreCase(name)) {
                    return ((ChatMessageContent) context).getAuthorRole().name();
                } else if ("content".equalsIgnoreCase(name)) {
                    return ((ChatMessageContent) context).getContent();
                }
            }
            return UNRESOLVED;
        }

        @Override
        public Object resolve(Object context) {
            if (context instanceof ChatMessageContent) {
                String content = ((ChatMessageContent) context).getContent();

                if (content == null) {
                    return UNRESOLVED;
                }

                return String.format(
                    "<message role=\"%s\">%s</message>",
                    ((ChatMessageContent) context).getAuthorRole().toString()
                        .toLowerCase(Locale.ROOT),
                    ContextVariableTypeConverter.escapeXmlString(content));
            }
            return UNRESOLVED;
        }

        @Override
        public Set<Entry<String, Object>> propertySet(Object context) {
            if (context instanceof ChatMessageContent) {
                HashMap<String, Object> result = new HashMap<>();
                result.put("role", ((ChatMessageContent) context).getAuthorRole().name());
                result.put("content", ((ChatMessageContent) context).getContent());
                return result.entrySet();
            }
            return new HashSet<>();
        }
    }

    private static class ContextVariableResolver implements ValueResolver {

        @Override
        public Object resolve(Object context, String name) {
            Object value = null;
            ContextVariable<?> variable = null;
            if (context instanceof KernelArguments) {
                variable = ((KernelArguments) context).get(name);
            } else if (context instanceof ContextVariable) {
                variable = ((ContextVariable<?>) context);
            }

            if (variable == null || variable.getValue() == null) {
                return UNRESOLVED;
            }

            value = variable.getValue();

            if (value instanceof Iterable) {
                return value;
            } else {
                // It is likely this will come escaped, but will be re escaped by the handlebars engine
                return promptString(variable);
            }
        }

        @Override
        public Object resolve(Object context) {
            if (context instanceof ContextVariable) {
                Object result = ((ContextVariable<?>) context).getValue();

                if (result == null) {
                    return UNRESOLVED;
                } else if (result instanceof Iterable) {
                    return result;
                } else {
                    return promptString(((ContextVariable<?>) context));
                }
            }
            return UNRESOLVED;
        }

        private String promptString(ContextVariable<?> context) {
            // This will come escaped, but will be re escaped by the handlebars engine
            return StringEscapeUtils.unescapeXml(context.toPromptString());
        }

        @Override
        public Set<Entry<String, Object>> propertySet(Object context) {
            if (context instanceof KernelArguments) {
                HashMap<String, Object> result = new HashMap<>();
                result.putAll((KernelArguments) context);
                return result.entrySet();
            } else if (context instanceof ContextVariable) {
                HashMap<String, Object> result = new HashMap<>();
                result.put("value", ((ContextVariable<?>) context).getValue());
                return result.entrySet();
            }
            return new HashSet<>();
        }
    }

    private class HandleBarsPromptTemplateHandler {

        private final String template;
        private final Handlebars handlebars;

        @SuppressFBWarnings("CT_CONSTRUCTOR_THROW") // Think this is a false positive
        public HandleBarsPromptTemplateHandler(
            Kernel kernel,
            String template,
            InvocationContext context) {
            this.template = template;
            this.handlebars = new Handlebars();
            this.handlebars
                .registerHelper("message", this::handleMessage)
                .registerHelper("each", handleEach(context))
                .with(EscapingStrategy.XML);

            addFunctionHelpers(kernel, this.handlebars, context);

            // TODO: 1.0 Add more helpers
        }

        private Helper<Object> handleEach(InvocationContext invocationContext) {
            return (variable, options) -> {
                if (variable instanceof ContextVariable) {
                    return ((ContextVariable<?>) variable)
                        .toPromptString(invocationContext.getContextVariableTypes());
                }

                if (variable instanceof Iterable) {
                    StringBuilder sb = new StringBuilder();

                    for (Object element : (Iterable<?>) variable) {
                        if (element instanceof KernelPlugin) {
                            KernelPlugin plugin = (KernelPlugin) element;
                            for (KernelFunction<?> function : plugin) {
                                sb.append(options.fn(function));
                            }
                        } else {
                            sb.append(options.fn(element));
                        }
                    }
                    return new Handlebars.SafeString(sb.toString());
                }

                ContextVariableType type = invocationContext.getContextVariableTypes()
                    .getVariableTypeForClass(variable.getClass());
                if (type != null) {
                    return type
                        .getConverter()
                        .toPromptString(invocationContext.getContextVariableTypes(), variable);
                }
                return null;
            };
        }

        @Nullable
        private CharSequence handleMessage(Object context, Options options)
            throws IOException {
            String role = options.hash("role");
            String content = (String) options.fn(context);

            if (context instanceof Optional) {
                ChatMessageContent message = ((Optional<ChatMessageContent>) context).orElse(
                    null);
                if (message != null) {
                    if (role == null || role.isEmpty()) {
                        role = message.getAuthorRole().name();
                    }
                    content = message.getContent();
                }
            }

            if (role != null && !role.isEmpty()) {
                return new Handlebars.SafeString(
                    String.format(
                        "<message role=\"%s\">%s</message>",
                        role.toLowerCase(Locale.ROOT),
                        content));
            }
            return null;
        }

        public Mono<String> render(KernelArguments variables) {
            try {
                ArrayList<ValueResolver> resolvers = new ArrayList<>();
                resolvers.add(new MessageResolver());
                resolvers.add(new ContextVariableResolver());

                if (promptTemplate.getPromptTemplateOptions()
                    .contains(PromptTemplateOption.ALLOW_CONTEXT_VARIABLE_METHOD_CALLS_UNSAFE)) {
                    resolvers.add(JavaBeanValueResolver.INSTANCE);
                }

                Context context = Context
                    .newBuilder(variables)
                    .resolver(resolvers.toArray(new ValueResolver[0]))
                    .build();

                String result = handlebars.compileInline(template).apply(context);
                return Mono.just(result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("StringSplitter")
    private static void addFunctionHelpers(Kernel kernel, Handlebars handlebars,
        InvocationContext context) {
        kernel
            .getPlugins()
            .forEach(plugin -> {
                plugin
                    .iterator()
                    .forEachRemaining(kernelFunction -> {
                        String functionName = kernelFunction.getName();
                        String pluginName = plugin.getName();
                        handlebars.registerHelper(
                            ToolCallBehavior.formFullFunctionName(pluginName, functionName),
                            functionInvokeHelper(kernel, kernelFunction, context));
                    });

            });
    }

    private static Helper<Object> functionInvokeHelper(
        Kernel kernel,
        KernelFunction<?> kernelFunction,
        InvocationContext invocationContext) {
        return (context, options) -> {

            KernelArguments.Builder builder = KernelArguments.builder();
            if (context instanceof KernelArguments) {
                builder.withVariables((KernelArguments) context);
            } else {
                builder.withInput(context);
            }

            if (options.hash(MAIN_KEY) != null) {
                builder.withVariables(options.hash
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> invocationContext
                            .getContextVariableTypes()
                            .contextVariableOf(entry.getValue()))));
            }

            // TODO Figure out if possible to do async render
            return kernelFunction
                .invokeAsync(kernel)
                .withArguments(builder.build())
                .block()
                .getResult();
        };
    }
}

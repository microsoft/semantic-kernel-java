// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableType;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionMetadata;
import com.microsoft.semantickernel.templateengine.semantickernel.TemplateException;
import com.microsoft.semantickernel.templateengine.semantickernel.TemplateException.ErrorCodes;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Represents a code block.
 */
public final class CodeBlock extends Block implements CodeRendering {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeBlock.class);
    private final List<Block> tokens;

    /**
     * Initializes a new instance of the {@link CodeBlock} class.
     *
     * @param tokens  The tokens.
     * @param content The content.
     */
    public CodeBlock(List<Block> tokens, String content) {
        super(content, BlockTypes.CODE);
        this.tokens = Collections.unmodifiableList(tokens);
    }

    @Override
    public boolean isValid() {
        Optional<Block> invalid = tokens.stream().filter(token -> !token.isValid()).findFirst();
        if (invalid.isPresent()) {
            LOGGER.error("Invalid block" + invalid.get().getContent());
            return false;
        }

        if (!this.tokens.isEmpty() && this.tokens.get(0).getType() == BlockTypes.NAMED_ARG) {
            LOGGER.error("Unexpected named argument found. Expected function name first.");
            return false;
        }

        if (this.tokens.size() > 1 && !this.isValidFunctionCall()) {
            return false;
        }

        return true;
    }

    private boolean isValidFunctionCall() {
        if (this.tokens.get(0).getType() != BlockTypes.FUNCTION_ID) {
            LOGGER.error("Unexpected second token found: " + tokens.get(1).getContent());
            return false;
        }

        if (this.tokens.get(1).getType() != BlockTypes.VALUE &&
            this.tokens.get(1).getType() != BlockTypes.VARIABLE &&
            this.tokens.get(1).getType() != BlockTypes.NAMED_ARG) {
            LOGGER.error(
                "The first arg of a function must be a quoted string, variable or named argument");
            return false;
        }

        for (int i = 2; i < this.tokens.size(); i++) {
            if (this.tokens.get(i).getType() != BlockTypes.NAMED_ARG) {
                LOGGER.error(
                    "Functions only support named arguments after the first argument. Argument " + i
                        + " is not named.");
                return false;
            }
        }

        return true;
    }

    @Override
    public Mono<String> renderCodeAsync(
        Kernel kernel,
        @Nullable KernelFunctionArguments arguments,
        @Nullable InvocationContext context) {
        if (!this.isValid()) {
            throw new TemplateException(ErrorCodes.SYNTAX_ERROR);
        }

        if (context == null) {
            context = InvocationContext.builder().build();
        }

        switch (this.tokens.get(0).getType()) {
            case VALUE:
            case VARIABLE:
                return Mono.just(
                    ((TextRendering) this.tokens.get(0)).render(context.getContextVariableTypes(),
                        arguments));

            case FUNCTION_ID:
                return this
                    .renderFunctionCallAsync(
                        (FunctionIdBlock) this.tokens.get(0),
                        kernel,
                        arguments,
                        context,
                        context.getContextVariableTypes().getVariableTypeForClass(String.class))
                    .map(ContextVariable::getValue)
                    .map(StringEscapeUtils::escapeXml11);

            case UNDEFINED:
            case TEXT:
            case CODE:
            default:
                throw new RuntimeException("Unknown type");
        }
    }

    private <T> Mono<ContextVariable<T>> renderFunctionCallAsync(
        FunctionIdBlock fBlock,
        Kernel kernel,
        @Nullable KernelFunctionArguments arguments,
        InvocationContext context,
        ContextVariableType<T> resultType) {

        // If the code syntax is {{functionName $varName}} use $varName instead of $input
        // If the code syntax is {{functionName 'value'}} use "value" instead of $input
        if (this.tokens.size() > 1) {
            //Cloning the original arguments to avoid side effects - arguments added to the original arguments collection as a result of rendering template variables.
            arguments = this.enrichFunctionArguments(kernel, fBlock,
                KernelFunctionArguments.builder().withVariables(arguments).build(),
                context);
        }

        return kernel
            .invokeAsync(
                fBlock.getPluginName(),
                fBlock.getFunctionName())
            .withArguments(arguments)
            .withResultType(resultType)
            .map(FunctionResult::getResultVariable);
    }

    /// <summary>
    /// Adds function arguments. If the first argument is not a named argument, it is added to the arguments collection as the 'input' argument.
    /// Additionally, for the prompt expression - {{MyPlugin.MyFunction p1=$v1}}, the value of the v1 variable will be resolved from the original arguments collection.
    /// Then, the new argument, p1, will be added to the arguments.
    /// </summary>
    /// <param name="kernel">Kernel instance.</param>
    /// <param name="fBlock">Function block.</param>
    /// <param name="arguments">The prompt rendering arguments.</param>
    /// <returns>The function arguments.</returns>
    /// <exception cref="KernelException">Occurs when any argument other than the first is not a named argument.</exception>
    private KernelFunctionArguments enrichFunctionArguments(
        Kernel kernel,
        FunctionIdBlock fBlock,
        KernelFunctionArguments arguments,
        @Nullable InvocationContext context) {
        Block firstArg = this.tokens.get(1);

        ContextVariableTypes types = context == null ? new ContextVariableTypes()
            : context.getContextVariableTypes();

        // Get the function metadata
        KernelFunctionMetadata<?> functionMetadata = kernel
            .getFunction(fBlock.getPluginName(), fBlock.getFunctionName()).getMetadata();

        // Check if the function has parameters to be set
        if (functionMetadata.getParameters().isEmpty()) {
            throw new SKException(
                "Function " + fBlock.getPluginName() + "." + fBlock.getFunctionName()
                    + " does not take any arguments but it is being called in the template with {this._tokens.Count - 1} arguments.");
        }

        String firstPositionalParameterName = null;
        Object firstPositionalInputValue = null;
        int namedArgsStartIndex = 1;

        if (firstArg.getType() != BlockTypes.NAMED_ARG) {
            // Gets the function first parameter name
            firstPositionalParameterName = functionMetadata.getParameters().get(0).getName();

            String contextVariableName = firstPositionalParameterName;
            if (firstArg instanceof VarBlock) {
                contextVariableName = ((VarBlock) firstArg).getName();
            }

            ContextVariable<?> arg = arguments.get(contextVariableName);
            Class<?> desiredType = functionMetadata.getParameters().get(0).getTypeClass();

            if (arg != null) {
                try {
                    firstPositionalInputValue = ContextVariable.convert(arg, desiredType, types);
                } catch (Exception e) {
                    // ignore
                }
            }

            if (firstPositionalInputValue == null) {
                firstPositionalInputValue = ((TextRendering) tokens.get(1)).render(types,
                    arguments);
                firstPositionalInputValue = ContextVariable
                    .convert(
                        firstPositionalInputValue,
                        functionMetadata.getParameters().get(0).getTypeClass(),
                        types);
            }

            // Type check is avoided and marshalling is done by the function itself
            if (firstPositionalInputValue == null) {
                throw new SKException(
                    "Unexpected null value for first positional argument: " + tokens.get(1)
                        .getContent());
            }

            // Keep previous trust information when updating the input
            arguments.put(
                firstPositionalParameterName,
                types.contextVariableOf(firstPositionalInputValue));
            namedArgsStartIndex++;
        }

        for (int i = namedArgsStartIndex; i < this.tokens.size(); i++) {
            // When casting fails because the block isn't a NamedArg, arg is null
            if (!(this.tokens.get(i) instanceof NamedArgBlock)) {
                throw new SKException("Unexpected first token type: {this._tokens[i].Type:G}");
            }

            NamedArgBlock arg = (NamedArgBlock) this.tokens.get(i);

            // Check if the positional parameter clashes with a named parameter
            if (firstPositionalParameterName != null &&
                firstPositionalParameterName.equalsIgnoreCase(arg.getName())) {
                throw new SKException(
                    "Ambiguity found as a named parameter '{arg.Name}' cannot be set for the first parameter when there is also a positional value: '{firstPositionalInputValue}' provided. Function: {fBlock.PluginName}.{fBlock.FunctionName}");
            }

            arguments.put(arg.getName(), types.contextVariableOf(arg.getValue(types, arguments)));
        }

        return arguments;
    }

}

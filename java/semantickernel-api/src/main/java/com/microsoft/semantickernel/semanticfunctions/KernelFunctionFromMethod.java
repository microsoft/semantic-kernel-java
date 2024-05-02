// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.semanticfunctions;

import static com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter.NO_DEFAULT_VALUE;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableType;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter.NoopConverter;
import com.microsoft.semantickernel.exceptions.AIException;
import com.microsoft.semantickernel.exceptions.AIException.ErrorCodes;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.hooks.FunctionInvokedEvent;
import com.microsoft.semantickernel.hooks.FunctionInvokingEvent;
import com.microsoft.semantickernel.hooks.KernelHooks;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A {@link KernelFunction} that is created from a method. This class is used to create a
 * {@link KernelFunction} from a method that is annotated with {@link DefineKernelFunction} and
 * {@link KernelFunctionParameter}.
 *
 * @param <T> the return type of the function
 */
public class KernelFunctionFromMethod<T> extends KernelFunction<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(KernelFunctionFromMethod.class);

    private final ImplementationFunc<T> function;

    private KernelFunctionFromMethod(
        ImplementationFunc<T> implementationFunc,
        @Nullable String pluginName,
        String functionName,
        @Nullable String description,
        @Nullable List<InputVariable> parameters,
        OutputVariable<?> returnParameter) {
        super(
            new KernelFunctionMetadata<>(
                pluginName,
                functionName,
                description,
                parameters,
                returnParameter),
            null);
        this.function = implementationFunc;
    }

    /**
     * Creates a new instance of {@link KernelFunctionFromMethod} from a method.
     *
     * @param method          the method to create the function from
     * @param target          the instance of the class that the method is a member of
     * @param pluginName      the name of the plugin which the function belongs to
     * @param functionName    the name of the function
     * @param description     the description of the function
     * @param parameters      the parameters of the function
     * @param returnParameter the return parameter of the function
     * @param <T>             the return type of the function
     * @return a new instance of {@link KernelFunctionFromMethod}
     */
    @SuppressWarnings("unchecked")
    public static <T> KernelFunction<T> create(
        Method method,
        Object target,
        @Nullable String pluginName,
        @Nullable String functionName,
        @Nullable String description,
        @Nullable List<InputVariable> parameters,
        @Nullable OutputVariable<?> returnParameter) {

        MethodDetails methodDetails = getMethodDetails(functionName, method, target);

        if (description == null || description.isEmpty()) {
            description = methodDetails.getDescription();
        }

        if (parameters == null || parameters.isEmpty()) {
            parameters = methodDetails.getParameters();
        }

        if (returnParameter == null) {
            returnParameter = methodDetails.getReturnParameter();
        }

        // unchecked cast
        return (KernelFunction<T>) new KernelFunctionFromMethod<>(
            methodDetails.getFunction(),
            pluginName,
            methodDetails.getName(),
            description,
            parameters,
            returnParameter);
    }

    private static MethodDetails getMethodDetails(
        @Nullable String functionName,
        Method method,
        Object target) {

        DefineKernelFunction annotation = method.getAnnotation(DefineKernelFunction.class);

        String description = null;
        String returnDescription = null;
        if (annotation != null) {
            if (!annotation.description().isEmpty()) {
                description = annotation.description();
            }
            if (!annotation.returnDescription().isEmpty()) {
                returnDescription = annotation.returnDescription();
            }
        }

        if (functionName == null || functionName.isEmpty()) {
            functionName = method.getName();
        }

        return new MethodDetails(
            functionName,
            description,
            getFunction(method, target),
            getParameters(method),
            new OutputVariable<>(
                returnDescription,
                method.getReturnType()));
    }

    @SuppressWarnings("unchecked")
    private static <T> ImplementationFunc<T> getFunction(Method method, Object instance) {
        return (kernel, function, arguments, variableType, invocationContext) -> {
            InvocationContext context;
            if (invocationContext == null) {
                context = InvocationContext.builder().build();
            } else {
                context = invocationContext;
            }

            // kernelHooks must be effectively final for lambda
            KernelHooks kernelHooks = context.getKernelHooks() != null
                ? context.getKernelHooks()
                : kernel.getGlobalKernelHooks();
            assert kernelHooks != null : "getGlobalKernelHooks() should never return null!";

            FunctionInvokingEvent updatedState = kernelHooks
                .executeHooks(
                    new FunctionInvokingEvent(function, arguments));
            KernelFunctionArguments updatedArguments = updatedState != null
                ? updatedState.getArguments()
                : arguments;

            try {
                List<Object> args = Arrays.stream(method.getParameters())
                    .map(getParameters(method, updatedArguments, kernel, context))
                    .collect(Collectors.toList());

                Mono<?> mono;
                try {
                    if (method.getReturnType().isAssignableFrom(Mono.class)) {
                        mono = (Mono<?>) method.invoke(instance, args.toArray());
                    } else {
                        mono = invokeAsyncFunction(method, instance, args);
                    }
                } catch (Exception e) {
                    return Mono.error(
                        new SKException("Function threw an exception: " + method.getName(), e));
                }

                return mono
                    .flatMap(it -> {
                        try {
                            return Mono.just((T) it);
                        } catch (ClassCastException e) {
                            return Mono.error(
                                new SKException("Return type does not match the expected type", e));
                        }
                    })
                    .map(it -> {
                        // If given a variable type, use it.
                        // If it's wrong, then it's a programming error on the part of the caller.
                        if (variableType != null) {
                            if (!variableType.getClazz().isAssignableFrom(it.getClass())) {
                                throw new SKException(String.format(
                                    "Return parameter type from %s.%s does not match the expected type %s",
                                    function.getPluginName(), function.getName(),
                                    it.getClass().getName()));
                            }
                            return new FunctionResult<>(
                                new ContextVariable<>(variableType, it), it);
                        }

                        Class<?> returnParameterType = function
                            .getMetadata()
                            .getOutputVariableType()
                            .getType();

                        // If the function has a return type that has a ContextVariableType<T>, use it.
                        ContextVariableType<T> contextVariableType = getContextVariableType(
                            context,
                            returnParameterType);
                        if (contextVariableType == null) {
                            // If getting the context variable type from the function fails, default to
                            // using the NoopConverter.
                            contextVariableType = getDefaultContextVariableType(
                                returnParameterType);
                        }

                        if (contextVariableType != null) {
                            return new FunctionResult<>(
                                new ContextVariable<>(contextVariableType, it),
                                it);
                        }

                        // If we get here, then either the returnParameterType doesn't match T
                        throw new SKException(String.format(
                            "Return parameter type from %s.%s does not match the expected type %s",
                            function.getPluginName(), function.getName(), it.getClass().getName()));

                    })
                    .map(it -> {
                        FunctionInvokedEvent<T> updatedResult = kernelHooks
                            .executeHooks(
                                new FunctionInvokedEvent<>(
                                    function,
                                    updatedArguments,
                                    it));
                        return updatedResult.getResult();
                    });
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> ContextVariableType<T> getContextVariableType(
        InvocationContext invocationContext, Class<?> clazz) {

        if (clazz != null) {
            try {
                // unchecked cast
                Class<T> tClazz = (Class<T>) clazz;
                ContextVariableType<T> type = invocationContext.getContextVariableTypes()
                    .getVariableTypeForClass(tClazz);
                return type;
            } catch (ClassCastException | SKException e) {
                // SKException is thrown from ContextVariableTypes.getDefaultVariableTypeForClass
                // if there is no default variable type for the class.
                // Fallthrough. Let the caller handle a null return.
            }
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> ContextVariableType<T> getDefaultContextVariableType(Class<?> clazz) {

        if (clazz != null) {
            try {
                // unchecked cast
                Class<T> tClazz = (Class<T>) clazz;
                ContextVariableTypeConverter<T> noopConverter = new NoopConverter<>(tClazz);

                return new ContextVariableType<>(noopConverter, tClazz);

            } catch (ClassCastException e) {
                // Fallthrough. Let the caller handle a null return.
            }
        }
        return null;
    }

    private static Mono<Object> invokeAsyncFunction(
        Method method, Object instance, List<Object> args) {
        return Mono.defer(
            () -> Mono.fromCallable(
                () -> {
                    try {
                        if (method.getReturnType().equals(void.class)
                            || method.getReturnType()
                                .equals(Void.class)) {
                            method.invoke(instance, args.toArray());
                            return null;
                        } else {
                            return method.invoke(instance, args.toArray());
                        }
                    } catch (InvocationTargetException e) {
                        throw new AIException(
                            ErrorCodes.INVALID_REQUEST,
                            "Function threw an exception: "
                                + method.getName(),
                            e.getCause());
                    } catch (IllegalAccessException e) {
                        throw new AIException(
                            ErrorCodes.INVALID_REQUEST,
                            "Unable to access function "
                                + method.getName(),
                            e);
                    }
                })
                .flatMap(it -> {
                    if (it == null) {
                        return Mono.empty();
                    } else {
                        return Mono.just(it);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic()));
    }

    @Nullable
    private static Function<Parameter, Object> getParameters(
        Method method,
        @Nullable KernelFunctionArguments context,
        Kernel kernel,
        InvocationContext invocationContext) {
        return parameter -> {
            if (KernelFunctionArguments.class.isAssignableFrom(parameter.getType())) {
                return context;
            } else if (Kernel.class.isAssignableFrom(parameter.getType())) {
                return kernel;
            } else {
                return getArgumentValue(method, context, parameter, kernel, invocationContext);
            }
        };
    }

    @Nullable
    private static Object getArgumentValue(
        Method method,
        @Nullable KernelFunctionArguments context,
        Parameter parameter,
        Kernel kernel,
        InvocationContext invocationContext) {
        String variableName = getGetVariableName(parameter);

        ContextVariable<?> arg = context == null ? null : context.get(variableName);

        // If there is 1 argument use "input" or the only argument
        if (arg == null && method.getParameters().length == 1) {
            if (context != null) {
                if (context.containsKey(KernelFunctionArguments.MAIN_KEY)) {
                    arg = context.get(KernelFunctionArguments.MAIN_KEY);
                } else if (context.size() == 1) {
                    arg = context.values().iterator().next();
                }
            }
        }

        if (arg == null) {
            KernelFunctionParameter annotation = parameter
                .getAnnotation(KernelFunctionParameter.class);
            if (annotation != null) {
                // Convert from the defaultValue, which is a String to the argument type
                // Expectation here is that the fromPromptString method will be able to handle a null or empty string
                Class<?> type = annotation.type();

                ContextVariableType<?> cvType = invocationContext
                    .getContextVariableTypes()
                    .getVariableTypeForClass(type);

                if (cvType != null) {
                    String defaultValue = annotation.defaultValue();
                    Object value = cvType.getConverter().fromPromptString(defaultValue);

                    arg = ContextVariable.convert(value, type,
                        invocationContext.getContextVariableTypes());
                }

                if (arg != null && NO_DEFAULT_VALUE.equals(arg.getValue())) {
                    if (!annotation.required()) {
                        return null;
                    }

                    throw new AIException(
                        AIException.ErrorCodes.INVALID_CONFIGURATION,
                        "Attempted to invoke function "
                            + method.getDeclaringClass().getName()
                            + "."
                            + method.getName()
                            + ". The context variable \""
                            + variableName
                            + "\" has not been set, and no default value is"
                            + " specified.");
                }
            }
        }

        if (arg == null && variableName.matches("arg\\d")) {
            LOGGER.warn(formErrorMessage(method, parameter));
        }

        if (arg != null && NO_DEFAULT_VALUE.equals(arg.getValue())) {
            if (parameter.getName().matches("arg\\d")) {
                throw new AIException(
                    AIException.ErrorCodes.INVALID_CONFIGURATION,
                    formErrorMessage(method, parameter));
            } else {
                throw new AIException(
                    AIException.ErrorCodes.INVALID_CONFIGURATION,
                    "Unknown arg " + parameter.getName());
            }
        }

        if (Kernel.class.isAssignableFrom(parameter.getType())) {
            return kernel;
        }

        KernelFunctionParameter annotation = parameter.getAnnotation(KernelFunctionParameter.class);
        if (annotation == null || annotation.type() == null) {
            return arg;
        }

        Class<?> type = annotation.type();

        if (!parameter.getType().isAssignableFrom(type)) {
            throw new AIException(
                AIException.ErrorCodes.INVALID_CONFIGURATION,
                "Annotation on method: " + method.getName() + " requested conversion to type: "
                    + type.getName() + ", however this cannot be assigned to parameter of type: "
                    + parameter.getType());
        }

        Object value = arg;

        if (arg != null) {

            if (parameter.getType().isAssignableFrom(arg.getType().getClazz())) {
                return arg.getValue();
            }

            if (isPrimitive(arg.getType().getClazz(), parameter.getType())) {
                return arg.getValue();
            }

            ContextVariableTypeConverter<?> c = arg.getType().getConverter();

            Object converted = c.toObject(invocationContext.getContextVariableTypes(),
                arg.getValue(), parameter.getType());
            if (converted != null) {
                return converted;
            }
        }

        // Well-known types only
        ContextVariableType<?> converter = invocationContext.getContextVariableTypes()
            .getVariableTypeForClass(type);
        if (converter != null) {
            try {
                value = converter.getConverter().fromObject(arg);
            } catch (NumberFormatException nfe) {
                throw new AIException(
                    AIException.ErrorCodes.INVALID_CONFIGURATION,
                    "Invalid value for "
                        + parameter.getName()
                        + " expected "
                        + type.getSimpleName()
                        + " but got "
                        + arg);
            }
        }

        if (value == null && type.equals(String.class) && arg != null) {
            ContextVariableTypeConverter c = arg.getType().getConverter();

            value = c.toPromptString(invocationContext.getContextVariableTypes(), arg.getValue());
        }

        return value;
    }

    @SuppressWarnings("OperatorPrecedence")
    private static boolean isPrimitive(Class<?> argType, Class<?> param) {
        return (argType == Byte.class || argType == byte.class) && (param == Byte.class
            || param == byte.class) ||
            (argType == Integer.class || argType == int.class) && (param == Integer.class
                || param == int.class)
            ||
            (argType == Long.class || argType == long.class) && (param == Long.class
                || param == long.class)
            ||
            (argType == Double.class || argType == double.class) && (param == Double.class
                || param == double.class)
            ||
            (argType == Float.class || argType == float.class) && (param == Float.class
                || param == float.class)
            ||
            (argType == Short.class || argType == short.class) && (param == Short.class
                || param == short.class)
            ||
            (argType == Boolean.class || argType == boolean.class) && (param == Boolean.class
                || param == boolean.class)
            ||
            (argType == Character.class || argType == char.class) && (param == Character.class
                || param == char.class);
    }

    private static String getGetVariableName(Parameter parameter) {
        KernelFunctionParameter annotation = parameter.getAnnotation(KernelFunctionParameter.class);

        if (annotation == null || annotation.name() == null || annotation.name().isEmpty()) {
            return parameter.getName();
        }
        return annotation.name();
    }

    private static String formErrorMessage(Method method, Parameter parameter) {
        Matcher matcher = Pattern.compile("arg(\\d)").matcher(parameter.getName());
        matcher.find();
        return "For the function "
            + method.getDeclaringClass().getName()
            + "."
            + method.getName()
            + ", the unknown parameter"
            + " name was detected as \""
            + parameter.getName()
            + "\" this is argument"
            + " number "
            + matcher.group(1)
            + " to the function, this indicates that the argument name for this function was"
            + " removed during compilation and semantic-kernel is unable to determine the name"
            + " of the parameter. To support this function the argument must be annotated with"
            + " @SKFunctionParameters or @SKFunctionInputAttribute. Alternatively the function"
            + " was invoked with a required context variable missing and no default value.";
    }

    private static List<InputVariable> getParameters(Method method) {
        return Arrays.stream(method
            .getParameters())
            .map(KernelFunctionFromMethod::toKernelParameterMetadata)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Nullable
    private static InputVariable toKernelParameterMetadata(Parameter parameter) {
        KernelFunctionParameter annotation = parameter.getAnnotation(
            KernelFunctionParameter.class);

        String name = parameter.getName();
        String description = null;
        String defaultValue = null;
        boolean isRequired = true;
        Class<?> type = parameter.getType();

        if (Kernel.class.isAssignableFrom(type) || KernelFunctionArguments.class.isAssignableFrom(
            type)) {
            return null;
        }
        if (annotation != null) {
            name = annotation.name();
            description = annotation.description();
            defaultValue = annotation.defaultValue();
            isRequired = annotation.required();
            type = annotation.type();
        }

        List<String> enumValues = getEnumOptions(type);

        return InputVariable.build(
            name,
            type,
            description,
            defaultValue,
            enumValues,
            isRequired);
    }

    public static @Nullable List<String> getEnumOptions(Class<?> type) {
        List<String> enumValues = null;
        if (type.isEnum()) {
            enumValues = Arrays.stream(type.getEnumConstants())
                .map(it -> {
                    return it.toString();
                })
                .collect(Collectors.toList());
        }
        return enumValues;
    }

    /**
     * A builder for {@link KernelFunction}.
     *
     * @param <T> the return type of the function
     * @return a new instance of {@link Builder}
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Concrete implementation of the abstract method in KernelFunction. {@inheritDoc}
     */
    @Override
    public Mono<FunctionResult<T>> invokeAsync(
        Kernel kernel,
        @Nullable KernelFunctionArguments arguments,
        @Nullable ContextVariableType<T> variableType,
        @Nullable InvocationContext invocationContext) {
        return function.invokeAsync(kernel, this, arguments, variableType, invocationContext);
    }

    /**
     * Concrete implementation of the abstract method in KernelFunction.
     */
    public interface ImplementationFunc<T> {

        /**
         * Invokes the function.
         *
         * @param kernel            the kernel to invoke the function on
         * @param function          the function to invoke
         * @param arguments         the arguments to the function
         * @param variableType      the variable type of the function
         * @param invocationContext the invocation context
         * @return a {@link Mono} that emits the result of the function invocation
         */
        Mono<FunctionResult<T>> invokeAsync(
            Kernel kernel,
            KernelFunction<T> function,
            @Nullable KernelFunctionArguments arguments,
            @Nullable ContextVariableType<T> variableType,
            @Nullable InvocationContext invocationContext);

        /**
         * Invokes the function.
         *
         * @param kernel            the kernel to invoke the function on
         * @param function          the function to invoke
         * @param arguments         the arguments to the function
         * @param variableType      the variable type of the function
         * @param invocationContext the invocation context
         * @return a {@link Mono} that emits the result of the function invocation
         */
        default FunctionResult<T> invoke(
            Kernel kernel,
            KernelFunction<T> function,
            @Nullable KernelFunctionArguments arguments,
            @Nullable ContextVariableType<T> variableType,
            @Nullable InvocationContext invocationContext) {
            return invokeAsync(kernel, function, arguments, variableType,
                invocationContext).block();
        }
    }

    /**
     * A builder for {@link KernelFunction}.
     */
    public static class Builder<T> {

        @Nullable
        private Method method;
        @Nullable
        private Object target;
        @Nullable
        private String pluginName;
        @Nullable
        private String functionName;
        @Nullable
        private String description;
        @Nullable
        private List<InputVariable> parameters;
        @Nullable
        private OutputVariable<?> returnParameter;

        /**
         * Sets the method to use to build the function.
         *
         * @param method the method to use
         * @return this instance of the {@link Builder} class
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder<T> withMethod(Method method) {
            this.method = method;
            return this;
        }

        /**
         * Sets the target to use to build the function.
         *
         * @param target the target to use
         * @return this instance of the {@link Builder} class
         */
        public Builder<T> withTarget(Object target) {
            this.target = target;
            return this;
        }

        /**
         * Sets the plugin name to use to build the function.
         *
         * @param pluginName the plugin name to use
         * @return this instance of the {@link Builder} class
         */
        public Builder<T> withPluginName(String pluginName) {
            this.pluginName = pluginName;
            return this;
        }

        /**
         * Sets the function name to use to build the function.
         *
         * @param functionName the function name to use
         * @return this instance of the {@link Builder} class
         */
        public Builder<T> withFunctionName(String functionName) {
            this.functionName = functionName;
            return this;
        }

        /**
         * Sets the description to use to build the function.
         *
         * @param description the description to use
         * @return this instance of the {@link Builder} class
         */
        public Builder<T> withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the parameters to use to build the function.
         *
         * @param parameters the parameters to use
         * @return this instance of the {@link Builder} class
         */
        public Builder<T> withParameters(List<InputVariable> parameters) {
            this.parameters = new ArrayList<>(parameters);
            return this;
        }

        /**
         * Sets the return parameter to use to build the function.
         *
         * @param returnParameter the return parameter to use
         * @return this instance of the {@link Builder} class
         */
        public Builder<T> withReturnParameter(OutputVariable<?> returnParameter) {
            this.returnParameter = returnParameter;
            return this;
        }

        /**
         * Builds a new instance of {@link KernelFunction}.
         *
         * @return a new instance of {@link KernelFunction}
         */
        public KernelFunction<T> build() {

            if (method == null) {
                throw new SKException(
                    "To build a KernelFunctionFromMethod, a method must be provided");
            }

            if (target == null) {
                throw new SKException(
                    "To build a plugin object must be provided");
            }

            return KernelFunctionFromMethod.create(
                method,
                target,
                pluginName,
                functionName,
                description,
                parameters,
                returnParameter);
        }

    }
}

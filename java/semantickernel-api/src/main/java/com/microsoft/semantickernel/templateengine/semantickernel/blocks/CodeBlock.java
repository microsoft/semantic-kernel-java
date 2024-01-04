// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.templateengine.semantickernel.blocks;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.orchestration.KernelFunctionMetadata;
import com.microsoft.semantickernel.orchestration.contextvariables.ContextVariable;
import com.microsoft.semantickernel.orchestration.contextvariables.ContextVariableType;
import com.microsoft.semantickernel.orchestration.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.contextvariables.DefaultKernelArguments;
import com.microsoft.semantickernel.orchestration.contextvariables.KernelArguments;
import com.microsoft.semantickernel.templateengine.semantickernel.TemplateException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public final class CodeBlock extends Block implements CodeRendering {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeBlock.class);
    private final List<Block> tokens;

    public CodeBlock(List<Block> tokens, String content) {
        super(content, BlockTypes.Code);
        this.tokens = Collections.unmodifiableList(tokens);
    }

    @Override
    public boolean isValid() {
        Optional<Block> invalid = tokens.stream().filter(token -> !token.isValid()).findFirst();
        if (invalid.isPresent()) {
            LOGGER.error("Invalid block" + invalid.get().getContent());
            return false;
        }

        if (this.tokens.size() > 1) {
            if (this.tokens.get(0).getType() != BlockTypes.FunctionId) {
                LOGGER.error("Unexpected second token found: " + this.tokens.get(1).getContent());
                return false;
            }

            if (this.tokens.get(1).getType() != BlockTypes.Value
                && this.tokens.get(1).getType() != BlockTypes.Variable) {
                LOGGER.error("Functions support only one parameter");
                return false;
            }
        }

        if (this.tokens.size() > 2) {
            LOGGER.error("Unexpected second token found: " + this.tokens.get(1).getContent());
            return false;
        }

        return true;
    }

    @Override
    public Mono<String> renderCodeAsync(Kernel kernel, @Nullable KernelArguments arguments) {
        if (!this.isValid()) {
            throw new TemplateException(TemplateException.ErrorCodes.SYNTAX_ERROR);
        }

        // this.Log.LogTrace("Rendering code: `{0}`", this.Content);

        switch (this.tokens.get(0).getType()) {
            case Value:
            case Variable:
                return Mono.just(
                    ((TextRendering) this.tokens.get(0)).render(arguments));

            case FunctionId:
                return this
                    .renderFunctionCallAsync(
                        (FunctionIdBlock) this.tokens.get(0),
                        kernel,
                        arguments,
                        ContextVariableTypes.getDefaultVariableTypeForClass(String.class))
                    .map(it -> {
                        return it.getValue();
                    });

            case Undefined:
            case Text:
            case Code:
            default:
                throw new RuntimeException("Unknown type");
        }
    }

    private <T> Mono<ContextVariable<T>> renderFunctionCallAsync(
        FunctionIdBlock fBlock,
        Kernel kernel,
        @Nullable KernelArguments arguments,
        ContextVariableType<T> resultType) {

        // If the code syntax is {{functionName $varName}} use $varName instead of $input
        // If the code syntax is {{functionName 'value'}} use "value" instead of $input
        if (this.tokens.size() > 1) {
            //Cloning the original arguments to avoid side effects - arguments added to the original arguments collection as a result of rendering template variables.
            arguments = this.enrichFunctionArguments(kernel, fBlock,
                arguments ==
                    null ? new DefaultKernelArguments() : new DefaultKernelArguments(arguments));
        }

        return kernel
            .invokeAsync(
                fBlock.getPluginName(),
                fBlock.getFunctionName(),
                arguments,
                resultType);
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
    private KernelArguments enrichFunctionArguments(Kernel kernel, FunctionIdBlock fBlock,
        KernelArguments arguments) {
        Block firstArg = this.tokens.get(1);

        // Get the function metadata
        KernelFunctionMetadata functionMetadata = kernel.getPlugins()
            .getFunction(fBlock.getPluginName(), fBlock.getFunctionName()).getMetadata();

        // Check if the function has parameters to be set
        if (functionMetadata.getParameters().size() == 0) {
            throw new IllegalArgumentException(
                "Function " + fBlock.getPluginName() + "." + fBlock.getFunctionName()
                    + " does not take any arguments but it is being called in the template with {this._tokens.Count - 1} arguments.");
        }

        String firstPositionalParameterName = null;
        Object firstPositionalInputValue = null;
        int namedArgsStartIndex = 1;

        if (firstArg.getType() != BlockTypes.NamedArg) {
            // Gets the function first parameter name
            firstPositionalParameterName = functionMetadata.getParameters().get(0).getName();

            firstPositionalInputValue = ((TextRendering) tokens.get(1)).render(arguments);
            // Type check is avoided and marshalling is done by the function itself

            // Keep previous trust information when updating the input
            arguments.put(firstPositionalParameterName,
                ContextVariable.of(firstPositionalInputValue));
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
                firstPositionalParameterName.toLowerCase(Locale.ROOT)
                    .equals(arg.getName().toLowerCase(Locale.ROOT))) {
                throw new SKException(
                    "Ambiguity found as a named parameter '{arg.Name}' cannot be set for the first parameter when there is also a positional value: '{firstPositionalInputValue}' provided. Function: {fBlock.PluginName}.{fBlock.FunctionName}");
            }

            arguments.put(arg.getName(), arg.getValue(arguments));
        }

        return arguments;
    }

}

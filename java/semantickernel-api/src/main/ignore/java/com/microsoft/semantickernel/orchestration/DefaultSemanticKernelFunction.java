// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration;

import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.ai.AIRequestSettings;
import com.microsoft.semantickernel.memory.NullMemory;
import com.microsoft.semantickernel.services.AIServiceSupplier;
import com.microsoft.semantickernel.skilldefinition.KernelSkillsSupplier;
import com.microsoft.semantickernel.skilldefinition.ParameterView;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

/// <summary>
/// Standard Semantic Kernel callable function.
/// SKFunction is used to extend one C# <see cref="Delegate"/>, <see cref="Func{T, TResult}"/>, <see
// cref="Action"/>,
/// with additional methods required by the kernel.
/// </summary>
public abstract class DefaultSemanticKernelFunction extends AbstractKernelFunction implements
    KernelFunction {

    public DefaultSemanticKernelFunction(
            List<ParameterView> parameters,
            String skillName,
            String functionName,
            String description,
            @Nullable KernelSkillsSupplier kernelSkillsSupplier,
            @Nullable AIServiceSupplier aiServiceSupplier) {
        super(
                parameters,
                skillName,
                functionName,
                description,
                Collections.emptyList(),
                kernelSkillsSupplier,
                aiServiceSupplier);
    }

    @Override
    public Mono<SKContext> invokeAsync(
            @Nullable String input, @Nullable SKContext context, @Nullable Object settings) {
        if (context == null) {
            assertSkillSupplierRegistered();
            context =
                    SKBuilders.context()
                            .withVariables(SKBuilders.variables().build())
                            .withMemory(NullMemory.getInstance())
                            .withSkills(super.getSkillsSupplier().get())
                            .build();
        } else {
            context = context.copy();
        }

        if (input != null) {
            context = context.update(input);
        }

        return this.invokeAsync(context, settings);
    }
}
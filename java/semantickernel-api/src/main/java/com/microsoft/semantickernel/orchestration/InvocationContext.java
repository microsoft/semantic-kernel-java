package com.microsoft.semantickernel.orchestration;

import com.microsoft.semantickernel.builders.Buildable;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.hooks.KernelHooks;
import com.microsoft.semantickernel.hooks.KernelHooks.UnmodifiableKernelHooks;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior.UnmodifiableToolCallBehavior;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nullable;

/**
 * Context passed to a Kernel or KernelFunction invoke
 */
public class InvocationContext implements Buildable {

    @Nullable
    private final UnmodifiableKernelHooks hooks;
    @Nullable
    private final PromptExecutionSettings promptExecutionSettings;
    @Nullable
    private final UnmodifiableToolCallBehavior toolCallBehavior;

    public InvocationContext(
        @Nullable KernelHooks hooks,
        @Nullable PromptExecutionSettings promptExecutionSettings,
        @Nullable ToolCallBehavior toolCallBehavior) {
        this.hooks = unmodifiableClone(hooks);
        this.promptExecutionSettings = promptExecutionSettings;
        this.toolCallBehavior = unmodifiableClone(toolCallBehavior);
    }

    public InvocationContext(InvocationContext context) {
        this.hooks = context.hooks;
        this.promptExecutionSettings = context.promptExecutionSettings;
        this.toolCallBehavior = context.toolCallBehavior;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "returns UnmodifiableKernelHooks")
    @Nullable
    public UnmodifiableKernelHooks getKernelHooks() {
        return hooks;
    }

    @Nullable
    public PromptExecutionSettings getPromptExecutionSettings() {
        return promptExecutionSettings;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "returns UnmodifiableToolCallBehavior")
    @Nullable
    public UnmodifiableToolCallBehavior getToolCallBehavior() {
        return toolCallBehavior;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Nullable
    private static UnmodifiableToolCallBehavior unmodifiableClone(
        @Nullable
        ToolCallBehavior toolCallBehavior) {
        if (toolCallBehavior instanceof UnmodifiableToolCallBehavior) {
            return (UnmodifiableToolCallBehavior) toolCallBehavior;
        } else if (toolCallBehavior != null) {
            return toolCallBehavior.unmodifiableClone();
        } else {
            return null;
        }
    }

    @Nullable
    private static UnmodifiableKernelHooks unmodifiableClone(
        @Nullable
        KernelHooks kernelHooks) {
        if (kernelHooks instanceof UnmodifiableKernelHooks) {
            return (UnmodifiableKernelHooks) kernelHooks;
        } else if (kernelHooks != null) {
            return kernelHooks.unmodifiableClone();
        } else {
            return null;
        }
    }

    public static class Builder implements SemanticKernelBuilder<InvocationContext> {

        @Nullable
        private UnmodifiableKernelHooks hooks;

        @Nullable
        private PromptExecutionSettings promptExecutionSettings;

        @Nullable
        private UnmodifiableToolCallBehavior toolCallBehavior;

        public Builder withKernelHooks(
            @Nullable KernelHooks hooks) {
            this.hooks = unmodifiableClone(hooks);
            return this;
        }

        public Builder withPromptExecutionSettings(
            @Nullable
            PromptExecutionSettings promptExecutionSettings) {
            this.promptExecutionSettings = promptExecutionSettings;
            return this;
        }

        public Builder withToolCallBehavior(
            @Nullable
            ToolCallBehavior toolCallBehavior) {
            this.toolCallBehavior = unmodifiableClone(toolCallBehavior);
            return this;
        }

        @Override
        public InvocationContext build() {
            return new InvocationContext(hooks, promptExecutionSettings, toolCallBehavior);
        }
    }

}

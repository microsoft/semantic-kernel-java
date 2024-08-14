// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.semanticfunctions;

public enum PromptTemplateOption {
    /**
     * Allow methods on objects provided as arguments to an invocation, to be invoked when rendering
     * a template and its return value used. Typically, this would be used to call a getter on an
     * object i.e. {@code {{#each users}} {{userName}} {{/each}} } on a handlebars template will
     * call the method {@code getUserName()} on each object in {@code users}.
     * <p>
     * WARNING: If this option is used, ensure that your template is trusted, and that objects added
     * as arguments to an invocation, do not contain methods that are unsafe to be invoked when
     * rendering a template.
     */
    ALLOW_CONTEXT_VARIABLE_METHOD_CALLS_UNSAFE
}
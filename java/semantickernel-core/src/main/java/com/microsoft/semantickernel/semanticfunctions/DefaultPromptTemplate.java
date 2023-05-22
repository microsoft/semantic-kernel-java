// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.semanticfunctions; // Copyright (c) Microsoft. All rights
// reserved.

import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.skilldefinition.ParameterView;
import com.microsoft.semantickernel.templateengine.PromptTemplateEngine;

import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// Prompt template.
/// </summary>
public class DefaultPromptTemplate implements PromptTemplate {
    private final String promptTemplate;
    private final PromptTemplateConfig config;

    public DefaultPromptTemplate(String promptTemplate, PromptTemplateConfig config) {
        this.promptTemplate = promptTemplate;
        this.config = config;
    }

    @Override
    public List<ParameterView> getParameters() {
        return new ArrayList<>();
    }

    @Override
    public Mono<String> renderAsync(
            SKContext executionContext, PromptTemplateEngine promptTemplateEngine) {
        return promptTemplateEngine.renderAsync(this.promptTemplate, executionContext);
    }

    /*
    private final String _template;
    private readonly IPromptTemplateEngine _templateEngine;

    // ReSharper disable once NotAccessedField.Local
    private readonly ILogger _log = NullLogger.Instance;

    // ReSharper disable once NotAccessedField.Local
    private readonly PromptTemplateConfig _promptConfig;

    /// <summary>
    /// Constructor for PromptTemplate.
    /// </summary>
    /// <param name="template">Template.</param>
    /// <param name="promptTemplateConfig">Prompt template configuration.</param>
    /// <param name="kernel">Kernel in which template is to take effect.</param>
    public PromptTemplate(string template, PromptTemplateConfig promptTemplateConfig, IKernel kernel)
        : this(template, promptTemplateConfig, kernel.PromptTemplateEngine, kernel.Log)
    {
    }

    /// <summary>
    /// Constructor for PromptTemplate.
    /// </summary>
    /// <param name="template">Template.</param>
    /// <param name="promptTemplateConfig">Prompt template configuration.</param>
    /// <param name="promptTemplateEngine">Prompt template engine.</param>
    /// <param name="log">Optional logger for prompt template.</param>
    public PromptTemplate(
        string template,
        PromptTemplateConfig promptTemplateConfig,
        IPromptTemplateEngine promptTemplateEngine,
        ILogger? log = null)
    {
        this._template = template;
        this._templateEngine = promptTemplateEngine;
        this._promptConfig = promptTemplateConfig;
        if (log != null) { this._log = log; }
    }

    /// <summary>
    /// Get the list of parameters used by the function, using JSON settings and template variables.
    /// TODO: consider caching results - though cache invalidation will add extra complexity
    /// </summary>
    /// <returns>List of parameters</returns>
    public IList<ParameterView> GetParameters()
    {
        var seen = new HashSet<string>(StringComparer.OrdinalIgnoreCase);

        // Parameters from config.json
        List<ParameterView> result = new();
        foreach (PromptTemplateConfig.InputParameter? p in this._promptConfig.Input.Parameters)
        {
            if (p == null) { continue; }

            result.Add(new ParameterView
            {
                Name = p.Name,
                Description = p.Description,
                DefaultValue = p.DefaultValue
            });

            seen.Add(p.Name);
        }

        // Parameters from the template
        List<VarBlock> listFromTemplate = this._templateEngine.ExtractBlocks(this._template)
            .Where(x => x.Type == BlockTypes.Variable)
            .Select(x => (VarBlock)x)
            .Where(x => x != null)
            .ToList();

        foreach (VarBlock x in listFromTemplate)
        {
            if (seen.Contains(x.Name)) { continue; }

            result.Add(new ParameterView { Name = x.Name });
            seen.Add(x.Name);
        }

        return result;
    }

    /// <summary>
    /// Render the template using the information in the context
    /// </summary>
    /// <param name="executionContext">Kernel execution context helpers</param>
    /// <returns>Prompt rendered to string</returns>
    public async Task<string> RenderAsync(SKContext executionContext)
    {
        return await this._templateEngine.RenderAsync(this._template, executionContext);
    }

     */
}

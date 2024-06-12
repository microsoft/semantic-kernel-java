// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples;

import com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.Example17_ChatGPT;
import com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.Example30_ChatWithPrompts;
import com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.Example33_Chat;
import com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.Example44_MultiChatCompletion;
import com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.Example63_ChatCompletionPrompts;
import com.microsoft.semantickernel.samples.syntaxexamples.configuration.Example08_RetryHandler;
import com.microsoft.semantickernel.samples.syntaxexamples.configuration.Example41_HttpClientUsage;
import com.microsoft.semantickernel.samples.syntaxexamples.configuration.Example58_ConfigureExecutionSettings;
import com.microsoft.semantickernel.samples.syntaxexamples.functions.Example01_NativeFunctions;
import com.microsoft.semantickernel.samples.syntaxexamples.functions.Example03_Arguments;
import com.microsoft.semantickernel.samples.syntaxexamples.functions.Example05_InlineFunctionDefinition;
import com.microsoft.semantickernel.samples.syntaxexamples.functions.Example09_FunctionTypes;
import com.microsoft.semantickernel.samples.syntaxexamples.functions.Example27_PromptFunctionsUsingChatGPT;
import com.microsoft.semantickernel.samples.syntaxexamples.functions.Example60_AdvancedMethodFunctions;
import com.microsoft.semantickernel.samples.syntaxexamples.plugins.Example10_DescribeAllPluginsAndFunctions;
import com.microsoft.semantickernel.samples.syntaxexamples.plugins.Example13_ConversationSummaryPlugin;
import com.microsoft.semantickernel.samples.syntaxexamples.template.Example06_TemplateLanguage;
import com.microsoft.semantickernel.samples.syntaxexamples.template.Example56_TemplateMethodFunctionsWithMultipleArguments;
import com.microsoft.semantickernel.samples.syntaxexamples.template.Example64_MultiplePromptTemplates;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Run all the syntax examples.
 * <p>
 * Refer to the <a href=
 * "https://github.com/microsoft/semantic-kernel/blob/experimental-java/java/samples/sample-code/README.md">
 * README</a> for configuring your environment to run the examples.
 */
public class RunAll {

    public static void main(String[] args) {
        List<MainMethod> mains = Arrays.asList(
            Example01_NativeFunctions::main,
            Example03_Arguments::main,
            Example05_InlineFunctionDefinition::main,
            Example06_TemplateLanguage::main,
            Example08_RetryHandler::main,
            Example09_FunctionTypes::main,
            Example10_DescribeAllPluginsAndFunctions::main,
            //Example11_WebSearchQueries::main,
            Example13_ConversationSummaryPlugin::main,
            Example17_ChatGPT::main,
            //Example26_AADAuth::main,

            Example27_PromptFunctionsUsingChatGPT::main,
            Example30_ChatWithPrompts::main,
            Example33_Chat::main,
            Example41_HttpClientUsage::main,
            Example43_GetModelResult::main,
            Example44_MultiChatCompletion::main,
            Example49_LogitBias::main,
            Example55_TextChunker::main,
            Example56_TemplateMethodFunctionsWithMultipleArguments::main,
            Example57_KernelHooks::main,
            Example58_ConfigureExecutionSettings::main,
            Example60_AdvancedMethodFunctions::main,
            Example61_MultipleLLMs::main,
            Example62_CustomAIServiceSelector::main,
            Example63_ChatCompletionPrompts::main,
            Example64_MultiplePromptTemplates::main,
            Example69_MutableKernelPlugin::main);

        Scanner scanner = new Scanner(System.in);
        mains.forEach(mainMethod -> {
            try {

                System.out.println("========================================");
                mainMethod.run(args);

                System.out.println("Press any key to continue...");
                scanner.nextLine();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public interface MainMethod {

        void run(String[] args) throws Exception;
    }
}

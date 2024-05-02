// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;

public class Example69_MutableKernelPlugin {

    /**
     * Mutable KernelPlugin example
     * <p>
     * KernelFunction can be added directly to the function collection of the KernelPlugin by using
     * KernelPlugin.getFunctions and putting the corresponding KernelFunction
     */
    public static void main(String[] args) throws NoSuchMethodException {
        System.out.println("======== Example69_MutableKernelPlugin ========");

        KernelPlugin plugin = new KernelPlugin("Plugin", "Mutable plugin", null);
        plugin.addFunction(KernelFunction.createFromMethod(
            Time.class.getMethod("date"), new Time())
            .withFunctionName("dateFunction")
            .build());

        Kernel kernel = Kernel.builder()
            .withPlugin(plugin)
            .build();

        var result = kernel.invokeAsync(kernel.getFunction("Plugin", "dateFunction"))
            .block();

        System.out.println("Result: " + result.getResult());
    }

    public static class Time {

        @DefineKernelFunction(name = "date")
        public String date() {
            return "2021-09-01";
        }
    }
}

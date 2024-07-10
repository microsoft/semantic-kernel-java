// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.semanticfunctions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import reactor.core.publisher.Mono;

public class KernelFunctionFromMethodTest {

    public KernelFunctionFromMethodTest() {
    }

    public static class ExamplePlugin {

        @DefineKernelFunction(name = "sqrt", description = "Take the square root of a number")
        public static double sqrt(
            @KernelFunctionParameter(name = "number1", description = "The number to take a square root of", type = double.class) double number1) {
            return Math.sqrt(number1);
        }
    }

    @Test
    void typeConversionOnMethodCall() {
        KernelPlugin plugin = KernelPluginFactory.createFromObject(
            new ExamplePlugin(), "ExamplePlugin");

        Kernel kernel = Kernel.builder().build();

        FunctionResult<String> result = plugin
            .get("sqrt")
            .invokeAsync(kernel)
            .withResultType(ContextVariableTypes.getGlobalVariableTypeForClass(String.class))
            .withArguments(
                KernelFunctionArguments.builder()
                    .withVariable("number1", "12.0")
                    .build())
            .block();

        Assertions.assertEquals("3.4641016151377544", result.getResult());
    }

    @Test
    void testCreate() throws Exception {
        Method method = String.class.getMethod("concat", String.class);
        String pluginName = "test-plugin";
        String functionName = "concat";
        String description = "concatenate two strings";
        List<InputVariable> parameters = Collections.singletonList(InputVariable.build(
            "string1", String.class, "first string to concatenate", null, null, true));
        OutputVariable<String> returnParameter = new OutputVariable<>(
            "concatenated strings", String.class);
        KernelFunction<?> result = KernelFunctionFromMethod.create(method, "test", pluginName,
            functionName, description, parameters, returnParameter);
        assertEquals(functionName, result.getName());
        assertEquals(pluginName, result.getPluginName());
        assertEquals(description, result.getDescription());
        assertEquals(parameters, result.getMetadata().getParameters());
        assertEquals(returnParameter, result.getMetadata().getOutputVariableType());
    }

    /**
     * Test of builder method, of class KernelFunctionFromMethod.
     */
    @Test
    void testBuilder() {
        String pluginName = null;
        String functionName = "concat";
        String description = "concatenate two strings";
        List<InputVariable> parameters = Collections
            .singletonList(new InputVariable("string1",
                "java.lang.String", "first string to concatenate", null, true, null));
        OutputVariable<?> returnParameter = new OutputVariable<>("java.lang.String",
            "concatenated strings");
        KernelFunction<String> result = new KernelFunctionFromPrompt.Builder()
            .withName(functionName)
            .withDescription(description)
            .withInputParameters(parameters)
            .withOutputVariable(returnParameter)
            .withPromptTemplate((kernel, args, context) -> Mono.empty())
            .build();
        assertEquals(functionName, result.getName());
        assertEquals(pluginName, result.getPluginName());
        assertEquals(description, result.getDescription());
        // TODO: This assert fails because getParameters is a List<KernelParameterMetadata<?>>, not an List<InputVariable>
        //       This feels like it's broken. Until this is fixed, we can compare the types
        // assertEquals(parameters, result.getMetadata().getParameters());
        assertEquals(parameters.size(), result.getMetadata().getParameters().size());
        for (int i = 0; i < parameters.size(); i++) {
            assertEquals(parameters.get(i).getDescription(),
                result.getMetadata().getParameters().get(i).getDescription());
            assertEquals(parameters.get(i).getName(),
                result.getMetadata().getParameters().get(i).getName());
            assertEquals(parameters.get(i).getType(),
                result.getMetadata().getParameters().get(i).getType());
            assertEquals(parameters.get(i).isRequired(),
                result.getMetadata().getParameters().get(i).isRequired());
        }
        assertEquals(returnParameter, result.getMetadata().getOutputVariableType());
        assertEquals(returnParameter.getType(),
            result.getMetadata().getOutputVariableType().getType());
        assertEquals(returnParameter.getDescription(),
            result.getMetadata().getOutputVariableType().getDescription());
    }

    /**
     * Test of invokeAsync method, of class KernelFunctionFromMethod.
     */
    @Test
    @Disabled("TODO: needs mocked http server")
    void testInvokeAsync() {
    }

    @TestFactory
    public Stream<DynamicTest> runInvocationConversionTests() {
        return Arrays.asList(
            new NoAnnotation(),
            new NoTypeOnAnnotation(),
            new PrimativeTypeOnAnnotation(),
            new SuperClassTypeTypeOnAnnotation(),
            new DefaultTypeOnAnnotation(),
            new StringTargetTypeOnAnnotation(),
            new ConvertUsingTargetType())
            .stream()
            .map(
                testClazz -> DynamicTest
                    .dynamicTest(
                        testClazz.getClass().getName() + "Test",
                        () -> {

                            ContextVariableTypeConverter<TargetClass> targetConverter = ContextVariableTypeConverter
                                .builder(TargetClass.class)
                                .fromObject(i -> {
                                    if (i instanceof SourceClass) {
                                        return new TargetClass(((SourceClass) i).value);
                                    }
                                    return (TargetClass) i;
                                })
                                .toPromptString(i -> null)
                                .build();

                            Boolean result = (Boolean) KernelFunctionFromMethod.createFromMethod(
                                testClazz.getMethod(),
                                testClazz)
                                .build()
                                .invoke(
                                    Kernel.builder().build(),
                                    testClazz.getArguments(),
                                    null,
                                    InvocationContext.builder()
                                        .withContextVariableConverter(targetConverter)
                                        .build())
                                .getResult();

                            Assertions.assertTrue(result);

                            testClazz.assertCalled();
                        }));
    }

    interface InvocationTest {

        Method getMethod() throws NoSuchMethodException;

        KernelFunctionArguments getArguments();

        void assertCalled();
    }

    @Nested
    class NoAnnotation implements InvocationTest {

        boolean called = false;

        @DefineKernelFunction
        public boolean method(Integer i) {
            called = i == 123;
            return called;
        }

        public Method getMethod() throws NoSuchMethodException {
            return this.getClass().getMethod("method", Integer.class);
        }

        @Override
        public KernelFunctionArguments getArguments() {
            return KernelFunctionArguments.builder()
                .withVariable("i", 123)
                .build();
        }

        @Override
        public void assertCalled() {
            Assertions.assertTrue(called);
        }
    }

    @Nested
    class NoTypeOnAnnotation implements InvocationTest {

        boolean called = false;

        @DefineKernelFunction
        public boolean method(
            @KernelFunctionParameter(name = "i") Integer i) {
            called = i == 123;
            return called;
        }

        public Method getMethod() throws NoSuchMethodException {
            return this.getClass().getMethod("method", Integer.class);
        }

        @Override
        public KernelFunctionArguments getArguments() {
            return KernelFunctionArguments.builder()
                .withVariable("i", 123)
                .build();
        }

        @Override
        public void assertCalled() {
            Assertions.assertTrue(called);
        }
    }

    @Nested
    class PrimativeTypeOnAnnotation implements InvocationTest {

        boolean called = false;

        @DefineKernelFunction
        public boolean method(
            @KernelFunctionParameter(name = "i", type = int.class) int i) {
            called = i == 123;
            return called;
        }

        public Method getMethod() throws NoSuchMethodException {
            return this.getClass().getMethod("method", int.class);
        }

        @Override
        public KernelFunctionArguments getArguments() {
            return KernelFunctionArguments.builder()
                .withVariable("i", 123)
                .build();
        }

        @Override
        public void assertCalled() {
            Assertions.assertTrue(called);
        }
    }

    @Nested
    class SuperClassTypeTypeOnAnnotation implements InvocationTest {

        boolean called = false;

        @DefineKernelFunction
        public boolean method(
            @KernelFunctionParameter(name = "i", type = List.class) List<Integer> i) {
            called = i.size() == 3;
            return called;
        }

        public Method getMethod() throws NoSuchMethodException {
            return this.getClass().getMethod("method", List.class);
        }

        @Override
        public KernelFunctionArguments getArguments() {
            return KernelFunctionArguments.builder()
                .withVariable("i", Arrays.asList(1, 2, 3))
                .build();
        }

        @Override
        public void assertCalled() {
            Assertions.assertTrue(called);
        }
    }

    @Nested
    class DefaultTypeOnAnnotation implements InvocationTest {

        boolean called = false;

        @DefineKernelFunction
        public boolean method(
            @KernelFunctionParameter(name = "i", type = int.class, defaultValue = "123") int i) {
            called = i == 123;
            return called;
        }

        public Method getMethod() throws NoSuchMethodException {
            return this.getClass().getMethod("method", int.class);
        }

        @Override
        public KernelFunctionArguments getArguments() {
            return KernelFunctionArguments.builder()
                .build();
        }

        @Override
        public void assertCalled() {
            Assertions.assertTrue(called);
        }
    }

    @Nested
    class StringTargetTypeOnAnnotation implements InvocationTest {

        boolean called = false;

        @DefineKernelFunction
        public boolean method(
            @KernelFunctionParameter(name = "i", type = String.class) String i) {
            called = i.equals("123");
            return called;
        }

        public Method getMethod() throws NoSuchMethodException {
            return this.getClass().getMethod("method", String.class);
        }

        @Override
        public KernelFunctionArguments getArguments() {

            ContextVariableTypeConverter<BigDecimal> dbConverter = ContextVariableTypeConverter
                .builder(BigDecimal.class)
                .fromObject(i -> (BigDecimal) i)
                .toPromptString(i -> null)
                .build();

            return KernelFunctionArguments.builder()
                .withVariable("i", new BigDecimal(123), dbConverter)
                .build();
        }

        @Override
        public void assertCalled() {
            Assertions.assertTrue(called);
        }
    }

    class TargetClass {

        final int value;

        TargetClass(int value) {
            this.value = value;
        }
    }

    class SourceClass {

        final int value;

        SourceClass(int value) {
            this.value = value;
        }
    }

    @Nested
    class ConvertUsingTargetType implements InvocationTest {

        boolean called = false;

        @DefineKernelFunction
        public boolean method(
            @KernelFunctionParameter(name = "i", type = TargetClass.class) TargetClass i) {
            called = i.value == 123;
            return called;
        }

        public Method getMethod() throws NoSuchMethodException {
            return this.getClass().getMethod("method", TargetClass.class);
        }

        @Override
        public KernelFunctionArguments getArguments() {

            ContextVariableTypeConverter<SourceClass> sourceConverter = ContextVariableTypeConverter
                .builder(SourceClass.class)
                .fromObject(i -> (SourceClass) i)
                .toPromptString(i -> null)
                .build();
            return KernelFunctionArguments.builder()
                .withVariable("i", new SourceClass(123), sourceConverter)
                .build();
        }

        @Override
        public void assertCalled() {
            Assertions.assertTrue(called);
        }
    }

}
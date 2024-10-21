// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.semantickernel.orchestration.responseformat.JsonSchemaResponseFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonSchemaTest {

    @Test
    public void jacksonGenerationTest() throws JsonProcessingException {
        JsonSchemaResponseFormat format = JsonSchemaResponseFormat.builder()
            .setResponseFormat(Foo.class)
            .setName("foo")
            .build();

        Assertions.assertEquals("foo", format.getJsonSchema().getName());

        Assertions.assertTrue(format.getJsonSchema().getSchema()
            .replaceAll("\\r\\n|\\r|\\n", "")
            .replaceAll(" +", "")
            .contains(
                "\"type\":\"object\",\"properties\":{\"bar\":{}}"));
    }

}

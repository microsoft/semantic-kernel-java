// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion.responseformat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.microsoft.semantickernel.orchestration.responseformat.ResponseSchemaGenerator;

public class JacksonResponseFormatGenerator implements ResponseSchemaGenerator {

    private final SchemaGenerator generator;

    public JacksonResponseFormatGenerator() {
        JacksonModule module = new JacksonModule();
        SchemaGeneratorConfigBuilder builder = new SchemaGeneratorConfigBuilder(
            SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
            .with(module);

        builder
            .forFields()
            .withRequiredCheck(fieldScope -> {
                return true;
            });

        generator = new SchemaGenerator(builder.build());
    }

    public JacksonResponseFormatGenerator(SchemaGenerator generator) {
        this.generator = generator;
    }

    @Override
    public String generateSchema(Class<?> clazz) {
        ObjectNode schema = generator.generateSchema(clazz);

        sanitize(schema);

        return schema.toPrettyString();
    }

    private static void sanitize(ContainerNode schema) {
        if (schema instanceof ObjectNode) {
            ((ObjectNode) schema).remove("$schema");

            if (schema.has("type") && schema.get("type").asText().equals("object")) {
                ((ObjectNode) schema).put("additionalProperties", false);
            }

            for (JsonNode node : (ObjectNode) schema) {
                if (node instanceof ContainerNode) {
                    sanitize((ContainerNode) node);
                }
            }
        } else if (schema instanceof ArrayNode) {
            for (JsonNode node : (ArrayNode) schema) {
                if (node instanceof ContainerNode) {
                    sanitize((ContainerNode) node);
                }
            }
        }
    }
}

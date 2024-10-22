// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion.responseformat;

import com.azure.ai.openai.models.ChatCompletionsResponseFormat;
import com.azure.json.JsonWriter;
import com.microsoft.semantickernel.orchestration.responseformat.JsonResponseSchema;
import java.io.IOException;

/**
 * Represents a response format for chat completions that uses a JSON schema.
 */
public class ChatCompletionsJsonSchemaResponseFormat extends ChatCompletionsResponseFormat {

    private final JsonResponseSchema schema;
    private String type = "json_schema";

    /**
     * Creates a new instance of the {@link ChatCompletionsJsonSchemaResponseFormat} class.
     *
     * @param schema The JSON schema.
     */
    public ChatCompletionsJsonSchemaResponseFormat(JsonResponseSchema schema) {
        this.schema = schema;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        jsonWriter.writeStartObject("json_schema");

        jsonWriter.writeBooleanField("strict", this.schema.isStrict());
        jsonWriter.writeStringField("name", this.schema.getName());

        jsonWriter.writeRawField("schema", this.schema.getSchema());
        jsonWriter.writeEndObject();
        return jsonWriter.writeEndObject();
    }

}

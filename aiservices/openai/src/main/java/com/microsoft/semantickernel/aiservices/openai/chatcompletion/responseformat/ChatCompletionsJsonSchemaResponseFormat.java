// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion.responseformat;

import com.azure.ai.openai.models.ChatCompletionsResponseFormat;
import com.azure.json.JsonWriter;
import com.microsoft.semantickernel.orchestration.responseformat.JsonResponseSchema;
import java.io.IOException;

public class ChatCompletionsJsonSchemaResponseFormat extends ChatCompletionsResponseFormat {

    private final JsonResponseSchema schema;
    private String type = "json_schema";

    public ChatCompletionsJsonSchemaResponseFormat(JsonResponseSchema schema) {
        this.schema = schema;
    }

    public String getType() {
        return this.type;
    }

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

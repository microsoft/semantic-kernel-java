package com.microsoft.semantickernel.samples.demos.lights;

import com.google.gson.Gson;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;

public class LightModelTypeConverter extends ContextVariableTypeConverter<LightModel> {
    private static final Gson gson = new Gson();

    public LightModelTypeConverter() {
        super(
            LightModel.class,
            obj -> {
                if(obj instanceof String) {
                    return gson.fromJson((String)obj, LightModel.class);
                } else {
                    return gson.fromJson(gson.toJson(obj), LightModel.class);
                }
            },
            (types, lightModel) -> gson.toJson(lightModel),
            json -> gson.fromJson(json, LightModel.class)
        );
    }
}

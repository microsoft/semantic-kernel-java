package withbrightness;// Copyright (c) Microsoft. All rights reserved.

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// <plugin>
public class LightsPlugin {

    // Mock data for the lights
    private final Map<Integer, LightModel> lights = new HashMap<>();

    public LightsPlugin() {
        lights.put(1, new LightModel(1, "Table Lamp", false, LightModel.Brightness.MEDIUM, "#FFFFFF"));
        lights.put(2, new LightModel(2, "Porch light", false, LightModel.Brightness.HIGH, "#FF0000"));
        lights.put(3, new LightModel(3, "Chandelier", true, LightModel.Brightness.LOW, "#FFFF00"));
    }

    @DefineKernelFunction(name = "get_lights", description = "Gets a list of lights and their current state")
    public List<LightModel> getLights() {
        System.out.println("Getting lights");
        return new ArrayList<>(lights.values());
    }

    @DefineKernelFunction(name = "change_state", description = "Changes the state of the light")
    public LightModel changeState(
            @KernelFunctionParameter(
                    name = "model",
                    description = "The new state of the model to set. Example model: " +
                            "{\"id\":99,\"name\":\"Head Lamp\",\"isOn\":false,\"brightness\":\"MEDIUM\",\"color\":\"#FFFFFF\"}",
                    type = LightModel.class) LightModel model
    ) {
        System.out.println("Changing light " + model.getId() + " " + model.getIsOn());
        if (!lights.containsKey(model.getId())) {
            throw new IllegalArgumentException("Light not found");
        }

        lights.put(model.getId(), model);

        return lights.get(model.getId());
    }
}
// </plugin>
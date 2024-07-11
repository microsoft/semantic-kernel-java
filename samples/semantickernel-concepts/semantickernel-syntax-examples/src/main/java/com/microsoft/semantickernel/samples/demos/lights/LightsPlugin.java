// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.demos.lights;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LightsPlugin {

    // Mock data for the lights
    private final List<LightModel> lights = new ArrayList<>();

    public LightsPlugin() {
        lights.add(new LightModel(1, "Table Lamp", false));
        lights.add(new LightModel(2, "Porch light", false));
        lights.add(new LightModel(3, "Chandelier", true));
    }

    @DefineKernelFunction(name = "get_lights", description = "Gets a list of lights and their current state")
    public List<LightModel> getLights() {
        System.out.println("Getting lights");
        return lights;
    }

    @DefineKernelFunction(name = "change_state", description = "Changes the state of the light")
    public LightModel changeState(
        @KernelFunctionParameter(name = "id", description = "The ID of the light to change") int id,
        @KernelFunctionParameter(name = "isOn", description = "The new state of the light") boolean isOn) {
        System.out.println("Changing light " + id + " " + isOn);
        Optional<LightModel> light = lights.stream()
            .filter(l -> l.getId() == id)
            .findFirst();

        if (light.isEmpty()) {
            throw new IllegalArgumentException("Light not found");
        }
        light.get().setIsOn(isOn);

        return light.get();
    }
}
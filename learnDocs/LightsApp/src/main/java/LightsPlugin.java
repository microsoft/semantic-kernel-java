// Copyright (c) Microsoft. All rights reserved.

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
    lights.put(1, new LightModel(1, "Table Lamp", false));
    lights.put(2, new LightModel(2, "Porch light", false));
    lights.put(3, new LightModel(3, "Chandelier", true));
  }

  @DefineKernelFunction(name = "get_lights", description = "Gets a list of lights and their current state")
  public List<LightModel> getLights() {
    System.out.println("Getting lights");
    return new ArrayList<>(lights.values());
  }

  @DefineKernelFunction(name = "change_state", description = "Changes the state of the light")
  public LightModel changeState(
      @KernelFunctionParameter(name = "id", description = "The ID of the light to change") int id,
      @KernelFunctionParameter(name = "isOn", description = "The new state of the light") boolean isOn) {
    System.out.println("Changing light " + id + " " + isOn);
    if (!lights.containsKey(id)) {
      throw new IllegalArgumentException("Light not found");
    }

    lights.get(id).setIsOn(isOn);

    return lights.get(id);
  }
}
// </plugin>
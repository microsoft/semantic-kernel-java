// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.demos.lights;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class LightModel {

    @JsonPropertyDescription("The unique identifier of the light")
    private int id;

    @JsonPropertyDescription("The name of the light")
    private String name;

    @JsonPropertyDescription("The state of the light")
    private Boolean isOn;

    public LightModel(int id, String name, Boolean isOn) {
        this.id = id;
        this.name = name;
        this.isOn = isOn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(Boolean isOn) {
        this.isOn = isOn;
    }
}

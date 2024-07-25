package withbrightness;
// Copyright (c) Microsoft. All rights reserved.

// <model>
public class LightModel {

    private int id;
    private String name;
    private Boolean isOn;
    private Brightness brightness;
    private String color;


    public enum Brightness {
        LOW,
        MEDIUM,
        HIGH
    }

    public LightModel(int id, String name, Boolean isOn, Brightness brightness, String color) {
        this.id = id;
        this.name = name;
        this.isOn = isOn;
        this.brightness = brightness;
        this.color = color;
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

    public Brightness getBrightness() {
        return brightness;
    }

    public void setBrightness(Brightness brightness) {
        this.brightness = brightness;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
// </model>
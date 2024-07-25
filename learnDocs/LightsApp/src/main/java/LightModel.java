// Copyright (c) Microsoft. All rights reserved.

// <model>
public class LightModel {

  private int id;
  private String name;
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
// </model>
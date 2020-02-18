package org.oppia.app.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Units {

  @SerializedName("name")
  @Expose
  private String name;
  @SerializedName("aliases")
  @Expose
  private List<String> aliases = null;
  @SerializedName("front_units")
  @Expose
  private List<String> frontUnits = null;
  @SerializedName("base_unit")
  @Expose
  private String baseUnit;

  public Units(String name, List<String> aliases, List<String> frontUnits, String baseUnit) {
    this.name = name;
    this.aliases = aliases;
    this.frontUnits = frontUnits;
    this.baseUnit = baseUnit;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public void setAliases(List<String> aliases) {
    this.aliases = aliases;
  }

  public List<String> getFrontUnits() {
    return frontUnits;
  }

  public void setFrontUnits(List<String> frontUnits) {
    this.frontUnits = frontUnits;
  }

  public String getBaseUnit() {
    return baseUnit;
  }

  public void setBaseUnit(String baseUnit) {
    this.baseUnit = baseUnit;
  }
}

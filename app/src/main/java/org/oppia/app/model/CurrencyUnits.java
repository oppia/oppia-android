package org.oppia.app.model;

import java.util.HashMap;

public class CurrencyUnits {
  public CurrencyUnits(HashMap<String, Units> unitHashMap) {
    this.unitHashMap = unitHashMap;
  }

  private HashMap<String, Units> unitHashMap;

  public HashMap<String, Units> getUnitHashMap() {
    return unitHashMap;
  }

  public void setUnitHashMap(HashMap<String, Units> unitHashMap) {
    this.unitHashMap = unitHashMap;
  }
}

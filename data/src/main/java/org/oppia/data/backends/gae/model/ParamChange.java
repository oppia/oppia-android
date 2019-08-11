package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class ParamChange {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> ParamChange
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> ParamChange
   */

  @Json(name = "generator_id")
  public String generatorId;

  @Json(name = "name")
  public String name;

  @Json(name = "customization_args")
  public CustomizationArgs customizationArgs;

}

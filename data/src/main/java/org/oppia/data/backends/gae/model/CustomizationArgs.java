package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public abstract class CustomizationArgs {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> ParamChange -> CustomizationArgs
   * Parent Hierarchy: ExplorationContainer -> Exploration -> ParamChange -> CustomizationArgs
   */

  @Json(name = "parse_with_jinja")
  public boolean parseWithJinja;

  //TODO: Check this free form data
  @Json(name = "value")
  public String value;

}

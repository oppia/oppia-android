package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class InteractionCustomizationArgs {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> CustomizationArgs
   */

  //TODO: Check this free form data
  @Json(name = "value")
  public String value;

}

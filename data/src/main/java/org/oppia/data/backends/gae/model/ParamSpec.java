package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class ParamSpec {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> ParamSpec
   */

  @Json(name = "obj_type")
  public String objType;

}

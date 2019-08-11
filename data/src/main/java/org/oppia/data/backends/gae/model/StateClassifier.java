package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class StateClassifier {

  /*
   * Parent Hierarchy: ExplorationContainer -> StateClassifier
   */

  @Json(name = "algorithm_id")
  public String algorithmId;

  //TODO: Check this free form data
  @Json(name = "classifier_data")
  public String classifierData;

  @Json(name = "data_schema_version")
  public int dataSchemaVersion;

}
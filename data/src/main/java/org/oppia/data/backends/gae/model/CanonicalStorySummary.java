package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class CanonicalStorySummary {

  /*
   * Parent Hierarchy: Topic -> CanonicalStorySummary
   */

  @Json(name = "id")
  public String id;

  @Json(name = "title")
  public String title;

  @Json(name = "description")
  public String description;

}

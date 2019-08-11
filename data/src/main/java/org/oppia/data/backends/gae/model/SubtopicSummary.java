package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class SubtopicSummary {

  /*
   * Parent Hierarchy: Topic -> SubtopicSummary
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works

  @Json(name = "id")
  public String id;

  @Json(name = "title")
  public String title;

  @Json(name = "skill_ids")
  public List<String> skillIds;

}

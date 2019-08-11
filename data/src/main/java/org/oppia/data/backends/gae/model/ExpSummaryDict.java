package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class ExpSummaryDict {

  /*
   * Parent Hierarchy: Story -> StoryNode -> ExpSummaryDict
   */

  //ImmutableList<> does not work but List<> works

  @Json(name = "title")
  public String title;

  @Json(name = "status")
  public String status;

}
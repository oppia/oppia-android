package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class SubtitledHtml {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> Hint
   */

  //TODO: HTML-String data type
  @Json(name = "html")
  public String html;

  @Json(name = "content_id")
  public String contentId;

}

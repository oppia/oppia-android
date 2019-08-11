package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class Hint {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> Hint
   */

  @Json(name = "hint_content")
  public SubtitledHtml hintContent;

}

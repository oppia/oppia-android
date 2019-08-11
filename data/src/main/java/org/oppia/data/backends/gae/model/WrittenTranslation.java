package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class WrittenTranslation {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> WrittenTranslations -> WrittenTranslation
   */

  //TODO: HTML-String data type
  @Json(name = "html")
  public String html;

  @Json(name = "needs_update")
  public boolean needsUpdate;

}

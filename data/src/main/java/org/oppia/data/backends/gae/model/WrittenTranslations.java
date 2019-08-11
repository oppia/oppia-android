package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.Map;

public class WrittenTranslations {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> WrittenTranslations
   */

  //TODO: ImmutableMap<> and Map<> error
  //ImmutableMap<> does not work but Map<> works
  //TODO: AutoJSON null data error

  @Json(name = "translations_mapping")
  public Map<String, Map<String, WrittenTranslation>> translationsMapping;

}

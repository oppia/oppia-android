package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.Map;

public class RecordedVoiceovers {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> RecordedVoiceovers
   */

  //TODO: ImmutableMap<> and Map<> error
  //ImmutableMap<> does not work but Map<> works

  @Json(name = "voiceovers_mapping")
  public Map<String, Map<String, Voiceover>> voiceoversMapping;

}

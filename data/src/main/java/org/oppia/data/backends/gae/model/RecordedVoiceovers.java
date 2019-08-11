package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

@AutoValue
public abstract class RecordedVoiceovers {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> RecordedVoiceovers
   */

  //TODO: ImmutableMap<> and Map<> error
  //ImmutableMap<> does not work but Map<> works
  //TODO: AutoJSON null data error

  @SerializedName("voiceovers_mapping")
  public abstract Map<String, Map<String, Voiceover>> getVoiceoversMapping();

  public static TypeAdapter<RecordedVoiceovers> createTypeAdapter(Gson gson) {
    return new AutoValue_RecordedVoiceovers.GsonTypeAdapter(gson);
  }

}

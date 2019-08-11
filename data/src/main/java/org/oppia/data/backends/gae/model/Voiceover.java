package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class Voiceover {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> RecordedVoiceovers -> Voiceover
   */

  //TODO: AutoJSON null data error

  @SerializedName("file_size_bytes")
  public abstract Long getFileSizeBytes();

  @SerializedName("needs_update")
  public abstract boolean isUpdatedNeeded();

  @SerializedName("filename")
  public abstract String getFilename();

  public static TypeAdapter<Voiceover> createTypeAdapter(Gson gson) {
    return new AutoValue_Voiceover.GsonTypeAdapter(gson);
  }

}

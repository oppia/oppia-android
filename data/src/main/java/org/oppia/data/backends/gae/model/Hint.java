package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class Hint {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> Hint
   */

  //TODO: AutoJSON null data error

  @SerializedName("hint_content")
  public abstract SubtitledHtml getHintContent();

  public static TypeAdapter<Hint> createTypeAdapter(Gson gson) {
    return new AutoValue_Hint.GsonTypeAdapter(gson);
  }

}

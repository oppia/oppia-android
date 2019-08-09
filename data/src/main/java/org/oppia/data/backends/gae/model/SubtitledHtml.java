package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class SubtitledHtml {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> Hint
   */

  //TODO: AutoJSON null data error

  //TODO: HTML-String data type
  @SerializedName("html")
  public abstract String getHtml();

  @SerializedName("content_id")
  public abstract String getContentId();

  public static TypeAdapter<SubtitledHtml> createTypeAdapter(Gson gson) {
    return new AutoValue_SubtitledHtml.GsonTypeAdapter(gson);
  }

}

package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class AdditionalStorySummary {

  /*
   * Parent Hierarchy: Topic -> AdditionalStorySummary
   */

  //TODO: AutoJSON null data error

  @SerializedName("id")
  public abstract String getId();

  @SerializedName("title")
  public abstract String getTitle();

  @SerializedName("description")
  public abstract String getDescription();

  public static TypeAdapter<AdditionalStorySummary> createTypeAdapter(Gson gson) {
    return new AutoValue_AdditionalStorySummary.GsonTypeAdapter(gson);
  }

}

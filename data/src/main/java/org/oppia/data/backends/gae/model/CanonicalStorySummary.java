package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class CanonicalStorySummary {

  /*
   * Parent Hierarchy: Topic -> CanonicalStorySummary
   */

  //TODO: AutoJSON null data error

  @SerializedName("id")
  public abstract String getId();

  @SerializedName("title")
  public abstract String getTitle();

  @SerializedName("description")
  public abstract String getDescription();

  public static TypeAdapter<CanonicalStorySummary> createTypeAdapter(Gson gson) {
    return new AutoValue_CanonicalStorySummary.GsonTypeAdapter(gson);
  }

}

package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class SubtopicSummary {

  /*
   * Parent Hierarchy: Topic -> SubtopicSummary
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works
  //TODO: AutoJSON null data error

  @SerializedName("id")
  public abstract String getId();

  @SerializedName("title")
  public abstract String getTitle();

  @SerializedName("skill_ids")
  public abstract List<String> getSkillIds();

  public static TypeAdapter<SubtopicSummary> createTypeAdapter(Gson gson) {
    return new AutoValue_SubtopicSummary.GsonTypeAdapter(gson);
  }

}

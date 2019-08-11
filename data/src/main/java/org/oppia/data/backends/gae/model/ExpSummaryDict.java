package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class ExpSummaryDict {

  /*
   * Parent Hierarchy: Story -> StoryNode -> ExpSummaryDict
   */

  //ImmutableList<> does not work but List<> works
  //TODO: AutoJSON null data error

  @SerializedName("title")
  public abstract String getTitle();

  @SerializedName("status")
  public abstract String getStory();

  public static TypeAdapter<ExpSummaryDict> createTypeAdapter(Gson gson) {
    return new AutoValue_ExpSummaryDict.GsonTypeAdapter(gson);
  }

}
package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class Story {

  /*
   * Parent Hierarchy: Story
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works
  //TODO: AutoJSON null data error

  @SerializedName("story_title")
  public abstract String getStoryTitle();

  @SerializedName("story_description")
  public abstract String getStoryDescription();

  @SerializedName("story_nodes")
  public abstract List<StoryNode> getStoryNodes();

  public static TypeAdapter<Story> createTypeAdapter(Gson gson) {
    return new AutoValue_Story.GsonTypeAdapter(gson);
  }

}

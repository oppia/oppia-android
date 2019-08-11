package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class Story {

  /*
   * Parent Hierarchy: Story
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works

  @Json(name = "story_title")
  public String storyTitle;

  @Json(name = "story_description")
  public String storyDescription;

  @Json(name = "story_nodes")
  public List<StoryNode> storyNodes;

}

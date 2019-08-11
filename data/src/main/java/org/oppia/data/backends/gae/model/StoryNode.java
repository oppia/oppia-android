package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class StoryNode {

  /*
   * Parent Hierarchy: Story -> StoryNode
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works

  @Json(name = "id")
  public String id;

  @Json(name = "title")
  public String title;

  @Json(name = "destination_node_ids")
  public List<String> destinationNodeIds;

  @Json(name = "acquired_skill_ids")
  public List<String> acquiredSkillIds;

  @Json(name = "prerequisite_skill_ids")
  public String prerequisiteSkillIds;

  @Json(name = "outline")
  public String outline;

  @Json(name = "outline_is_finalized")
  public boolean outlineIsFinalized;

  @Json(name = "exploration_id")
  public String explorationId;

  @Json(name = "exp_summary_dict")
  public ExpSummaryDict expSummaryDict;

  @Json(name = "completed")
  public boolean completed;

}
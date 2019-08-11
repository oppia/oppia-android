package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class StoryNode {

  /*
   * Parent Hierarchy: Story -> StoryNode
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works
  //TODO: AutoJSON null data error

  @SerializedName("id")
  public abstract String getId();

  @SerializedName("title")
  public abstract String getTitle();

  @SerializedName("destination_node_ids")
  public abstract List<String> getDestinationNodeIds();

  @SerializedName("acquired_skill_ids")
  public abstract List<String> getAcquiredSkillIds();

  @SerializedName("prerequisite_skill_ids")
  public abstract List<String> getAPrerequisiteSkillIds();

  @SerializedName("outline")
  public abstract String getOutline();

  //TODO: Check method name
  @SerializedName("outline_is_finalized")
  public abstract boolean isOutlineFinalized();

  @SerializedName("exploration_id")
  public abstract String getExplorationId();

  @SerializedName("exp_summary_dict")
  public abstract ExpSummaryDict getExplorationSummaryDict();

  @SerializedName("completed")
  public abstract boolean isCompleted();

  public static TypeAdapter<StoryNode> createTypeAdapter(Gson gson) {
    return new AutoValue_StoryNode.GsonTypeAdapter(gson);
  }

}
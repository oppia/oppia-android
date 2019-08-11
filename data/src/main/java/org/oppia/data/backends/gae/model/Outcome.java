package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class Outcome {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> Outcome
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> AnswerGroup -> Outcome
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works

  @Json(name = "dest")
  public String dest;

  @Json(name = "refresher_exploration_id")
  public String refresherExplorationId;

  @Json(name = "feedback")
  public SubtitledHtml feedback;

  @Json(name = "param_changes")
  public List<ParamChange> paramChanges;

  @Json(name = "missing_prerequisite_skill_id")
  public String missingPrerequisiteSkillId;

  @Json(name = "labelled_as_correct")
  public boolean labelledAsCorrect;

}

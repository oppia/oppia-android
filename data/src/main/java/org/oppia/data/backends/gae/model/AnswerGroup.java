package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class AnswerGroup {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> AnswerGroup
   */

  @Json(name = "tagged_skill_misconception_id")
  public String taggedSkillMisconceptionId;

  @Json(name = "outcome")
  public Outcome outcome;

  @Json(name = "rule_specs")
  public List<RuleSpec> ruleSpecs;

  //TODO : Free-form data
  @Json(name = "training_data")
  public String trainingData;


}

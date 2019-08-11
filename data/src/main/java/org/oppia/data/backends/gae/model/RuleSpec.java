package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.Map;

public class RuleSpec {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> AnswerGroups -> RuleSpec
   */

  //TODO: ImmutableMap<> and Map<> error
  //ImmutableMap<> does not work but Map<> works

  //TODO: Map<String, *> : Check this free form data
  @Json(name = "inputs")
  public Map<String, String> inputs;

  @Json(name = "rule_type")
  public String rule_type;

}

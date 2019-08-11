package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;
import java.util.Map;

public class Exploration {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration
   */

  /*
   * Ignore below params
   * language_code
   */

  //TODO: ImmutableMap<> and Map<> error
  //ImmutableMap<> does not work but Map<> works
  //TODO: ImmutableList<> and List<> error

  @Json(name = "states")
  public Map<String, State> states;

  @Json(name = "param_changes")
  public List<ParamChange> paramChanges;

  @Json(name = "param_specs")
  public Map<String, ParamSpec> paramSpecs;

  @Json(name = "init_state_name")
  public String initStateName;

  @Json(name = "objective")
  public String objective;

  @Json(name = "correctness_feedback_enabled")
  public boolean correctnessFeedbackEnabled;

  @Json(name = "title")
  public String title;

}

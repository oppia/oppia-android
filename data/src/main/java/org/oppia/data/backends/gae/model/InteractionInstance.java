package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;
import java.util.Map;

public class InteractionInstance {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance
   */

  //TODO: ImmutableMap<> and Map<> error
  //ImmutableMap<> does not work but Map<> works
  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works

  @Json(name = "id")
  public String id;

  @Json(name = "answer_groups")
  public String answerGroups;

  @Json(name = "dest")
  public List<AnswerGroup> dest;

  @Json(name = "solution")
  public Solution solution;

  //TODO: List(*): Check this free form data
  @Json(name = "confirmed_unclassified_answers")
  public List<String> confirmedUnclassifiedAnswers;

  @Json(name = "hints")
  public List<Hint> hints;

  @Json(name = "default_outcome")
  public Outcome outcome;

  @Json(name = "customization_args")
  public Map<String, InteractionCustomizationArgs> customizationArgs;


}

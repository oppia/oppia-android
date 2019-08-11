package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class Solution {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> Solution
   */

  @Json(name = "interaction_id")
  public String interactionId;

  @Json(name = "answer_is_exclusive")
  public boolean answerIsExclusive;

  @Json(name = "correct_answer")
  public String correctAnswer;

  @Json(name = "explanation")
  public SubtitledHtml explanation;

}

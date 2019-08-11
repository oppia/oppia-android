package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class Question {

  /*
   * Parent Hierarchy: QuestionPlayer -> Question
   */

  @Json(name = "id")
  public String id;

  @Json(name = "question_state_data")
  public State state;

  @Json(name = "version")
  public int version;

}
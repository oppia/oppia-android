package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class QuestionPlayer {

  /*
   * Parent Hierarchy: QuestionPlayer
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works

  @Json(name = "question_dicts")
  public List<Question> questions;

}

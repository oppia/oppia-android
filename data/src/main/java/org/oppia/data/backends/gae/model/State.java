package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class State {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works

  @Json(name = "recorded_voiceovers")
  public RecordedVoiceovers recordedVoiceovers;

  @Json(name = "content")
  public SubtitledHtml content;

  @Json(name = "written_translations")
  public WrittenTranslations writtenTranslations;

  @Json(name = "param_changes")
  public List<ParamChange> paramChanges;

  @Json(name = "classifier_model_id")
  public String classifierModelId;

  @Json(name = "interaction")
  public InteractionInstance interaction;

  @Json(name = "solicit_answer_details")
  public boolean solicitAnswerDetails;

}
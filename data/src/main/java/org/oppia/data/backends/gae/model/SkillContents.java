package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class SkillContents {

  /*
   * Parent Hierarchy: ConceptCard -> SkillContents
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works

  @Json(name = "worked_examples")
  public List<SubtitledHtml> workedExamples;

  @Json(name = "recorded_voiceovers")
  public RecordedVoiceovers recordedVoiceovers;

  @Json(name = "explanation")
  public SubtitledHtml explanation;

  @Json(name = "written_translations")
  public WrittenTranslations writtenTranslations;

}

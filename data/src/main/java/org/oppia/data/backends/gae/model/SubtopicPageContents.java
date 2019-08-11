package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class SubtopicPageContents {

  /*
   * Parent Hierarchy: Subtopic -> SubtopicPageContents
   */

  @Json(name = "subtitled_html")
  public SubtitledHtml subtitledHtml;

  @Json(name = "recorded_voiceovers")
  public RecordedVoiceovers recordedVoiceovers;

  @Json(name = "content")
  public SubtitledHtml content;

  @Json(name = "written_translations")
  public WrittenTranslations writtenTranslations;

}

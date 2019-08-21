package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubtopicPageContents(

  @Json(name = "subtitled_html") val content: SubtitledHtml,
  @Json(name = "recorded_voiceovers") val recordedVoiceovers: RecordedVoiceovers,
  @Json(name = "written_translations") val writtenTranslations: WrittenTranslations

)

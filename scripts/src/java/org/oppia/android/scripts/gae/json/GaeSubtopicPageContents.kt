package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeSubtopicPageContents(
  @Json(name = "subtitled_html") val subtitledHtml: GaeSubtitledHtml,
  @Json(name = "recorded_voiceovers") val recordedVoiceovers: GaeRecordedVoiceovers,
  @Json(name = "written_translations") val writtenTranslations: GaeWrittenTranslations
)

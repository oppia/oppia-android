package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeSkillContents(
  @Json(name = "explanation") val explanation: GaeSubtitledHtml,
  @Json(name = "recorded_voiceovers") val recordedVoiceovers: GaeRecordedVoiceovers,
  @Json(name = "written_translations") val writtenTranslations: GaeWrittenTranslations
)

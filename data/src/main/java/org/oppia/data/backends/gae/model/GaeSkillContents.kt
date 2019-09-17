package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for SkillContents model
 *
 */
@JsonClass(generateAdapter = true)
data class GaeSkillContents(

  @Json(name = "explanation") val explanation: GaeSubtitledHtml,
  @Json(name = "worked_examples") val workedExamples: List<GaeSubtitledHtml>,
  @Json(name = "recorded_voiceovers") val recordedVoiceovers: GaeRecordedVoiceovers,
  @Json(name = "written_translations") val writtenTranslations: GaeWrittenTranslations

)

package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SkillContents(

  @Json(name ="explanation") val explanation: SubtitledHtml?,
  @Json(name ="worked_examples") val workedExamples: List<SubtitledHtml>?,
  @Json(name ="recorded_voiceovers") val recordedVoiceovers: RecordedVoiceovers?,
  @Json(name ="written_translations") val writtenTranslations: WrittenTranslations?

  )

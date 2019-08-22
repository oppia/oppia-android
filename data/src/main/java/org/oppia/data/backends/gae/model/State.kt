package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class State(

  @Json(name = "recorded_voiceovers") val recordedVoiceovers: RecordedVoiceovers?,
  @Json(name = "content") val content: SubtitledHtml?,
  @Json(name = "written_translations") val writtenTranslations: WrittenTranslations?,
  @Json(name = "param_changes") val paramChanges: List<ParamChange>?,
  @Json(name = "classifier_model_id") val classifierModelId: String?,
  @Json(name = "interaction") val interactionInstance: InteractionInstance?,
  @Json(name = "solicit_answer_details") val isSolicitAnswerDetails: Boolean?

)

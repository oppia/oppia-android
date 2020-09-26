package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for State model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L1774
 */
@JsonClass(generateAdapter = true)
data class GaeState(

  @Json(name = "recorded_voiceovers") val recordedVoiceovers: GaeRecordedVoiceovers?,
  @Json(name = "content") val content: GaeSubtitledHtml?,
  @Json(name = "written_translations") val writtenTranslations: GaeWrittenTranslations?,
  @Json(name = "param_changes") val paramChanges: List<GaeParamChange>?,
  @Json(name = "classifier_model_id") val classifierModelId: String?,
  @Json(name = "interaction") val interactionInstance: GaeInteractionInstance?,
  @Json(name = "solicit_answer_details") val isSolicitAnswerDetails: Boolean?

)

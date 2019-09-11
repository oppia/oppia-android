package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Solution model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L221
 */
@JsonClass(generateAdapter = true)
data class GaeSolution(

  @Json(name = "interaction_id") val interactionId: String?,
  @Json(name = "answer_is_exclusive") val isAnswerExclusive: Boolean?,
  @Json(name = "correct_answer") val correctAnswer: String?,
  @Json(name = "explanation") val explanation: GaeSubtitledHtml?

)

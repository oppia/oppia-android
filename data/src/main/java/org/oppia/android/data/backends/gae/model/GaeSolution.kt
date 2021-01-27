package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Solution model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/state_domain.py#L338
 */
@JsonClass(generateAdapter = true)
data class GaeSolution(

  @Json(name = "interaction_id") val interactionId: String?,
  @Json(name = "answer_is_exclusive") val isAnswerExclusive: Boolean?,
  @Json(name = "correct_answer") val correctAnswer: String?,
  @Json(name = "explanation") val explanation: GaeSubtitledHtml?

)

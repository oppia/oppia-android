package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Solution(

  @Json(name = "interaction_id") val interactionId: String?,
  @Json(name = "answer_is_exclusive") val isAnswerExclusive: Boolean?,
  @Json(name = "correct_answer") val correctAnswer: String?,
  @Json(name = "explanation") val explanation: SubtitledHtml?

)

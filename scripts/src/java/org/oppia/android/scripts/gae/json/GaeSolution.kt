package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeSolution(
  @Json(name = "answer_is_exclusive") val answerIsExclusive: Boolean,
  @Json(name = "correct_answer")
  @GaeInteractionObject.SolutionInteractionAnswer
  val correctAnswer: GaeInteractionObject,
  @Json(name = "explanation") val explanation: GaeSubtitledHtml
)

package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for InteractionInstance model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L277
 */
@JsonClass(generateAdapter = true)
data class GaeInteractionInstance(

  @Json(name = "id") val id: String?,
  @Json(name = "answer_groups") val answerGroups: List<GaeAnswerGroup>?,
  @Json(name = "solution") val solution: GaeSolution?,
  @Json(name = "confirmed_unclassified_answers") val confirmedUnclassifiedAnswers: List<Any?>?,
  @Json(name = "hints") val hints: List<GaeHint?>?,
  @Json(name = "default_outcome") val outcome: GaeOutcome?,
  @Json(name = "customization_args") val customizationArgs: Map<String, GaeCustomizationArgs>?

)

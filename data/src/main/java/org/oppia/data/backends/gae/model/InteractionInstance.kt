package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InteractionInstance(

  @Json(name = "id") val id: String?,
  @Json(name = "answer_groups") val answerGroups: List<AnswerGroup>?,
  @Json(name = "solution") val solution: Solution?,
  @Json(name = "confirmed_unclassified_answers") val confirmedUnclassifiedAnswers: List<Any?>?,
  @Json(name = "hints") val hints: List<Hint?>?,
  @Json(name = "default_outcome") val outcome: Outcome?,
  @Json(name = "customization_args") val customizationArgs: Map<String, CustomizationArgs>?

)

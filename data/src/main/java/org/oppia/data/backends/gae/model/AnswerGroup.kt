package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnswerGroup(

  @Json(name = "tagged_skill_misconception_id") val taggedSkillMisconceptionId: String?,
  @Json(name = "outcome") val outcome: Outcome?,
  @Json(name = "rule_specs") val ruleSpecs: List<RuleSpec?>?,
  @Json(name = "training_data") val trainingData: List<Any?>

)

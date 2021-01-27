package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for AnswerGroup model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/state_domain.py#L67
 */
@JsonClass(generateAdapter = true)
data class GaeAnswerGroup(

  @Json(name = "tagged_skill_misconception_id") val taggedSkillMisconceptionId: String?,
  @Json(name = "outcome") val outcome: GaeOutcome?,
  @Json(name = "rule_specs") val ruleSpecs: List<GaeRuleSpec?>?,
  @Json(name = "training_data") val trainingData: List<Any?>

)

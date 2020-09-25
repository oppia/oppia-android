package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for RuleSpec model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L1113
 */
@JsonClass(generateAdapter = true)
data class GaeRuleSpec(

  @Json(name = "inputs") val inputs: Map<String, Any>?,
  @Json(name = "rule_type") val ruleType: String?

)

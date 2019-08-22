package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RuleSpec(

  @Json(name = "inputs") val inputs: Map<String, Any>?,
  @Json(name = "rule_type") val ruleType: String?

)

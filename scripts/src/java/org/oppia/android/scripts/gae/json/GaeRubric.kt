package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeRubric(
  @Json(name = "difficulty") val difficulty: String,
  @Json(name = "explanations") val explanations: List<String>
)

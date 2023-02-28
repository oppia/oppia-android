package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeMisconception(
  @Json(name = "id") val id: Int,
  @Json(name = "name") val name: String,
  @Json(name = "notes") val notes: String,
  @Json(name = "feedback") val feedback: String,
  @Json(name = "must_be_addressed") val mustBeAddressed: Boolean
)

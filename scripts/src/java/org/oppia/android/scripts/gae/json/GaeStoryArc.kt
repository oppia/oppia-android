package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeStoryArc(
  @Json(name = "id") val id: String,
  @Json(name = "title") val title: String,
  @Json(name = "description") val description: String,
  @Json(name = "node_ids") val nodeIds: List<String>
)

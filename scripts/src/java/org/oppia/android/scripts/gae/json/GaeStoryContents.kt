package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeStoryContents(
  @Json(name = "nodes") val nodes: List<GaeStoryNode>,
  @Json(name = "initial_node_id") val initialNodeId: String?,
  @Json(name = "next_node_id") val nextNodeId: String
)

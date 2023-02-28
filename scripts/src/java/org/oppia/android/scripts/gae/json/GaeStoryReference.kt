package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeStoryReference(
  @Json(name = "story_id") val storyId: String,
  @Json(name = "story_is_published") val storyIsPublished: Boolean
)

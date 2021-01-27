package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Story model
 * @link https://github.com/oppia/oppia/blob/develop/core/controllers/story_viewer.py#L77
 */
@JsonClass(generateAdapter = true)
data class GaeStory(

  @Json(name = "story_title") val storyTitle: String?,
  @Json(name = "story_description") val storyDescription: String?,
  @Json(name = "story_nodes") val storyNodes: List<GaeStoryNode>?

)

package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for story summary model with minimum details
 * https://github.com/oppia/oppia/blob/b33aa9/core/domain/story_domain.py#L1118
 */
@JsonClass(generateAdapter = true)
data class GaeStorySummary(

  @Json(name = "id") val storyId: String?,
  @Json(name = "title") val title: String?,
  @Json(name = "description") val description: String?

)

package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Model for serialization/deserialization using Moshi with Retrofit
 * @see <a href="https://github.com/oppia/oppia/blob/b33aa9cf1aa6372e12d0b35f95cceb44efe5320f/core/domain/story_domain.py#L1118">Oppia-Web StorySummary structure</a>
 */
@JsonClass(generateAdapter = true)
data class StorySummary(

  @Json(name = "id") val storyId: String?,
  @Json(name = "title") val title: String?,
  @Json(name = "description") val description: String?

)

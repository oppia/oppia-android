package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Subtopic model
 * https://github.com/oppia/oppia/blob/b33aa9/core/controllers/subtopic_viewer.py#L31
 */
@JsonClass(generateAdapter = true)
data class GaeSubtopic(

  @Json(name = "subtopic_title") val subtopicTitle: String?,
  @Json(name = "page_contents") val pageContents: GaeSubtopicPageContents?

)

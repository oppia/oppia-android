package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Subtopic model
 * @link https://github.com/oppia/oppia/blob/develop/core/controllers/subtopic_viewer.py#L74
 */
@JsonClass(generateAdapter = true)
data class GaeSubtopic(

  @Json(name = "subtopic_title") val subtopicTitle: String?,
  @Json(name = "page_contents") val pageContents: GaeSubtopicPageContents?

)

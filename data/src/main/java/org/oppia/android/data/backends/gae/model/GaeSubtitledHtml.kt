package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for SubtitledHtml model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/state_domain.py#L2138
 */
@JsonClass(generateAdapter = true)
data class GaeSubtitledHtml(

  @Json(name = "html") val html: String?,
  @Json(name = "content_id") val contentId: String?

)

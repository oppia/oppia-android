package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Data class for SubtitledHtml model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L1243
 */
@JsonClass(generateAdapter = true)
data class GaeSubtitledHtml(

  @Json(name = "content_id") val contentId: String?,
  @Json(name = "html") val html: String?

) : Serializable

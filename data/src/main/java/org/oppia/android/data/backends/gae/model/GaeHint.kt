package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Hint model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/state_domain.py#L274
 */
@JsonClass(generateAdapter = true)
data class GaeHint(

  @Json(name = "hint_content") val hintContent: GaeSubtitledHtml?

)

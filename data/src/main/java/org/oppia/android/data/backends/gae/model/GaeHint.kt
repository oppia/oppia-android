package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Hint model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L163
 */
@JsonClass(generateAdapter = true)
data class GaeHint(

  @Json(name = "hint_content") val hintContent: GaeSubtitledHtml?

)

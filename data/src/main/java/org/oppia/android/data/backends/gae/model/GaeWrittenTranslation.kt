package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for WrittenTranslation model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L749
 */
@JsonClass(generateAdapter = true)
data class GaeWrittenTranslation(

  @Json(name = "html") val html: String?,
  @Json(name = "needs_update") val isUpdateNeeded: Boolean?

)

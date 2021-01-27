package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for WrittenTranslation model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/state_domain.py#L1461
 */
@JsonClass(generateAdapter = true)
data class GaeWrittenTranslation(

  @Json(name = "translation") val translation: String?,
  @Json(name = "data_format") val dataFormat: String?,
  @Json(name = "needs_update") val isUpdateNeeded: Boolean?

)

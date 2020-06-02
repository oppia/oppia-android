package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for WrittenTranslations model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L803
 */
@JsonClass(generateAdapter = true)
data class GaeWrittenTranslations(

  @Json(name = "translations_mapping") val translationsMapping:
  Map<String, Map<String, GaeWrittenTranslation>>?

)

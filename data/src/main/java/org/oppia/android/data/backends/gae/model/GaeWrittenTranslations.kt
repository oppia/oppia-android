package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for WrittenTranslations model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/state_domain.py#L1519
 */
@JsonClass(generateAdapter = true)
data class GaeWrittenTranslations(

  @Json(name = "translations_mapping")
  val translationsMapping: Map<String, Map<String, GaeWrittenTranslation>>?

)

package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WrittenTranslations(

  @Json(name = "translations_mapping") val translationsMapping: Map<String, Map<String, WrittenTranslation>>

)

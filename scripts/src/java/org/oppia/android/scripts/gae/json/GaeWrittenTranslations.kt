package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeWrittenTranslations(
  @Json(name = "translations_mapping")
  val translationsMapping: Map<String, Map<String, GaeWrittenTranslation>>
)

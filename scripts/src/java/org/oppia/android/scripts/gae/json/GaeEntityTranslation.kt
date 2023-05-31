package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeEntityTranslation(
  @Json(name = "entity_id") val entityId: String,
  @Json(name = "entity_type") val entityType: String,
  @Json(name = "entity_version") override val version: Int,
  @Json(name = "language_code") val languageCode: String,
  @Json(name = "translations") val translations: Map<String, GaeTranslatedContent>
): VersionedStructure

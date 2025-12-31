package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a feature flag configuration that can be received from Google App Engine.
 *
 * @property name the remote name for this flag
 * @property isEnabled whether the server indicates this flag's feature should be enabled
 */
@JsonClass(generateAdapter = true)
data class GaeFeatureFlag(
  @Json(name = "name") val name: String,
  @Json(name = "enabled") val isEnabled: Boolean,
)

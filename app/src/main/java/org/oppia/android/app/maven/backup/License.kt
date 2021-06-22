package org.oppia.android.app.maven.backup

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class that stores the license names and links for the dependencies whose license
 * links can't be extracted from their POM files.
 */
@JsonClass(generateAdapter = true)
data class License(
  @Json(name = "artifact_name") val artifactName: String,
  @Json(name = "license_names") val licenseNames: MutableList<String>,
  @Json(name = "license_links") val licenseLinks: MutableList<String>
)
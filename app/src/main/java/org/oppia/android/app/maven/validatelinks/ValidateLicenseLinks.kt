package org.oppia.android.app.maven.validatelinks

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class to store the list of all possible license links. */
@JsonClass(generateAdapter = true)
data class ValidateLicenseLinks(
  @Json(name = "license_links") val licenseLinks: Set<LicenseLink>
)
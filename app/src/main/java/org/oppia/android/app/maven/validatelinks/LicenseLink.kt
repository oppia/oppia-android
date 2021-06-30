package org.oppia.android.app.maven.validatelinks

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class to determine whether a license link is valid or not. */
@JsonClass(generateAdapter = true)
data class LicenseLink(
  @Json(name = "link") val link: String,
  @Json(name = "is_valid") val isValid: Boolean?
)



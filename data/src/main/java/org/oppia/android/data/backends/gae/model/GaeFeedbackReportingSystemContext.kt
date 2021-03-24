package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for feedback reporting device information model
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingSystemContext(

  /** The package version name for the user's instance of the app. */
  @Json(name = "version_name") val packageVersionName: String,
  /** The package version code for the user's instance of the app. */
  @Json(name = "package_version_code") val packageVersionCode: Int,
  /** The ISO-639 code for the country locale set on the user's device. */
  @Json(name = "country_locale") val countryLocale: String,
  /** The IDO-3166 code for the language locale set on the user's device. */
  @Json(name = "language_locale") val languageLocale: String

)

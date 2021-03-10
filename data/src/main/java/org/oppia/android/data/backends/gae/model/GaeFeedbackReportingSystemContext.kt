package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for feedback reporting device information model
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingSystemContext(

  @Json(name = "package_version_name") val packageVersionName: String,
  @Json(name = "package_version_code") val packageVersionCode: Int,
  @Json(name = "country_locale") val countryLocale: String,
  @Json(name = "language_locale") val languageLocale: String

)

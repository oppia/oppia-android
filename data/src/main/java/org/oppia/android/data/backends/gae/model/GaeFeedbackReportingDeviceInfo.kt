package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for feedback reporting device information model
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingDeviceInfo (

  @Json(name = "package_name") val package_name: String?,
  @Json(name = "package_code") val package_code: String?,
  @Json(name = "country_locale") val country_locale: String?,
  @Json(name = "language_locale") val language_locale: String?

)
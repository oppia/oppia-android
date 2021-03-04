package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for feedback reporting device build model
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingDeviceContext(

  @Json(name = "device_model") val device_model: String?,
  @Json(name = "sdk_version") val sdk_version: Int?,
  @Json(name = "device_brand") val device_brand: String?,
  @Json(name = "build_fingerprint") val build_fingerprint: String?,
  @Json(name = "network_type") val network_type: String?,

)

package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for feedback reporting device build model
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingDeviceContext(

  @Json(name = "device_model") val deviceModel: String?,
  @Json(name = "sdk_version") val sdkVersion: Int?,
  @Json(name = "device_brand") val deviceBrand: String?,
  @Json(name = "build_fingerprint") val buildFingerprint: String?,
  @Json(name = "network_type") val networkType: String?,

)

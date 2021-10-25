package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class for feedback reporting device build model. */
// TODO(#3016): Link backend domain model
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingDeviceContext(

  /** The model code of the user's device. */
  @Json(name = "device_model") val deviceModel: String,
  /** The SDK version code on the user's device. */
  @Json(name = "sdk_version") val sdkVersion: Int,
  /** The unique fingerprint ID of the specific device build. */
  @Json(name = "build_fingerprint") val buildFingerprint: String,
  /** Corresponds to the type of network connection the device is on (wifi, cellular, or unspecified). */
  @Json(name = "network_type") val networkType: String

)

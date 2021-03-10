package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.oppia.android.data.backends.gae.NetworkApiKey
import javax.inject.Inject

/**
 * Data class for the feedback report sent by the Android app to remote storage.
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeFeedbackReport constructor(

  @Json(name = "api_key") val apiKey: String,
  @Json(name = "report_creation_timestamp_ms") val reportCreationTimestampMs: Double?,
  @Json(name = "user_supplied_feedback") val userSuppliedFeedback: GaeUserSuppliedFeedback?,
  @Json(name = "system_context") val systemContext: GaeFeedbackReportingSystemContext?,
  @Json(name = "device_context") val deviceContext: GaeFeedbackReportingDeviceContext?,
  @Json(name = "app_context") val appContext: GaeFeedbackReportingAppContext?

)

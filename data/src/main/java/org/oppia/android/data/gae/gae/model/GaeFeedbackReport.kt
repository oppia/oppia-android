package org.oppia.android.data.gae.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for the feedback report sent by the Android app to remote storage.
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeFeedbackReport(

  /** The date and time in ms that the report was created. */
  @Json(name = "report_creation_timestamp_ms") val reportCreationTimestampMs: Long,
  /** The information that user's provide in the feedback report. */
  @Json(name = "user_supplied_feedback") val userSuppliedFeedback: GaeUserSuppliedFeedback,
  /** Information collected about the user device's system. */
  @Json(name = "system_context") val systemContext: GaeFeedbackReportingSystemContext,
  /** Information collected about the user's physical device build. */
  @Json(name = "device_context") val deviceContext: GaeFeedbackReportingDeviceContext,
  /** Information collected about the user's specific app experience. */
  @Json(name = "app_context") val appContext: GaeFeedbackReportingAppContext

)

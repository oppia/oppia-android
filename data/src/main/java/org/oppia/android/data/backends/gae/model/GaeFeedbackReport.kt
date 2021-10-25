package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class for the feedback report sent by the Android app to remote storage. */
// TODO(#3016): Link backend domain model
@JsonClass(generateAdapter = true)
data class GaeFeedbackReport(

  /** The current version of the feedback report used. */
  @Json(name = "android_report_info_schema_version") val schemaVersion: Int,
  /** The date and time in ms that the report was created. */
  @Json(name = "report_submission_timestamp_sec") val reportSubmissionTimestampSec: Int,
  /** The information that user's provide in the feedback report. */
  @Json(name = "user_supplied_feedback") val userSuppliedFeedback: GaeUserSuppliedFeedback,
  /** Information collected about the user device's system. */
  @Json(name = "system_context") val systemContext: GaeFeedbackReportingSystemContext,
  /** Information collected about the user's physical device build. */
  @Json(name = "device_context") val deviceContext: GaeFeedbackReportingDeviceContext,
  /** Information collected about the user's specific app experience. */
  @Json(name = "app_context") val appContext: GaeFeedbackReportingAppContext

)

package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingForm (
  @Json(name = "report_type") val report_type: String?,
  @Json(name = "category") val category: String?,
  @Json(name = "feedback_list") val feedback_list: List<String>?
)
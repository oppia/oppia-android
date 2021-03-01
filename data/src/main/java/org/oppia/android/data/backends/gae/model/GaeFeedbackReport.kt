package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeFeedbackReport (
  @Json(name = "api_key") val api_key: String?,
  @Json(name = "feedback_form") val feedback_form: GaeFeedbackReportingForm?
)
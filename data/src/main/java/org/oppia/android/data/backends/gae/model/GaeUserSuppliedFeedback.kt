package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for feedback reporting form model
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeUserSuppliedFeedback(

  /** The type of feedback report that the user is filling out (suggestion, issue, or crash report). */
  @Json(name = "report_type") val reportType: String,
  /** The category selected by the user to provide feedback for, based on the report type. */
  @Json(name = "category") val category: String,
  /** Corresponds to checkbox options that a user selects in this report. */
  @Json(name = "feedback_list") val feedbackList: List<String>?,
  /** Text input that a user might provide if they choose "other" as a category or feedback list option. */
  @Json(name = "open_text_user_input") val openTextUserInput: String?

)

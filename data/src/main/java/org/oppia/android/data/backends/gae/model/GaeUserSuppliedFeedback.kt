package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for feedback reporting form model
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeUserSuppliedFeedback (

  @Json(name = "report_type") val report_type: String?,
  @Json(name = "category") val category: String?,
  @Json(name = "feedback_list") val feedback_list: List<String>?,
  @Json(name = "user_input") val user_input: String?

)

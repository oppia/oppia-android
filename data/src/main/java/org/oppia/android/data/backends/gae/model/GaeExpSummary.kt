package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for ExpSummary model
 * https://github.com/oppia/oppia/blob/b56a20/core/domain/summary_services.py#L340
 */
@JsonClass(generateAdapter = true)
data class GaeExpSummary(

  @Json(name = "title") val title: String?,
  @Json(name = "status") val status: String?,
  @Json(name = "category") val category: String?,
  @Json(name = "objective") val objective: String?,
  @Json(name = "num_views") val numViews: Int?,
  @Json(name = "activity_type") val activityType: String?,
  @Json(name = "id") val id: String?,
  @Json(name = "created_on_msec") val createdOnMsec: Double?,
  @Json(name = "last_updated_msec") val lastUpdatedMsec: Double?,
  @Json(name = "ratings") val ratings: Map<String, Float>?,
  @Json(name = "tags") val tags: List<String>?

)

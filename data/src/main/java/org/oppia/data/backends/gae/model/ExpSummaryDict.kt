package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExpSummaryDict(

  /*
   * Ignore below params
   * community_owned, human_readable_contributors_summary,
   * thumbnail_bg_color, thumbnail_icon_url, language_code
   */

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

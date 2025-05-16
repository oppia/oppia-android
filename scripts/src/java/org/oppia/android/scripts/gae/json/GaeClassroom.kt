package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeClassroom(
  @Json(name = "classroom_id") val id: String,
  @Json(name = "name") val name: String,
  @Json(name = "url_fragment") val urlFragment: String,
  @Json(name = "topic_id_to_prerequisite_topic_ids")
  val topicIdToPrereqTopicIds: Map<String, List<String>>,
  @Json(name = "course_details") val courseDetails: String,
  @Json(name = "topic_list_intro") val topicListIntro: String,
  @Json(name = "teaser_text") val teaserText: String,
  @Json(name = "is_published") val isPublished: Boolean,
  @Json(name = "thumbnail_data") val thumbnailData: Any,
  @Json(name = "banner_data") val bannerData: Any,
  @Json(name = "diagnostic_test_is_enabled") val diagnosticTestIsEnabled: Boolean,
  @Json(name = "index") val index: Int
)

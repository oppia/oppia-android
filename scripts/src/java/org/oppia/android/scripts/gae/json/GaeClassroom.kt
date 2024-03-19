package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeClassroom(
  @Json(name = "name") val name: String,
  @Json(name = "url_fragment") val urlFragment: String,
  @Json(name = "topic_ids") val topicIds: List<String>,
  @Json(name = "course_details") val courseDetails: String,
  @Json(name = "topic_list_intro") val topicListIntro: String
)

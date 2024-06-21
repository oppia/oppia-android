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
  @Json(name = "topic_list_intro") val topicListIntro: String
)

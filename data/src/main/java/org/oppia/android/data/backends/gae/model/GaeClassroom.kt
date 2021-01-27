package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Classroom model containing full information
 * @link https://github.com/oppia/oppia/blob/develop/core/controllers/classroom.py#L67
 */
@JsonClass(generateAdapter = true)
data class GaeClassroom(

  @Json(name = "topic_summary_dicts") val topicSummaryDicts: List<GaeTopicSummary>?,
  @Json(name = "topic_list_intro") val topicListIntro: String?,
  @Json(name = "course_details") val courseDetails: String?,
  @Json(name = "name") val name: String?
)

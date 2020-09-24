package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Classroom model containing full information
 * https://github.com/oppia/oppia/blob/77d6fd/core/controllers/classroom.py#L49
 */
@JsonClass(generateAdapter = true)
data class GaeClassroom(

  @Json(name = "topic_summary_dicts") val topicSummaryDicts: List<GaeTopicSummary>?

)

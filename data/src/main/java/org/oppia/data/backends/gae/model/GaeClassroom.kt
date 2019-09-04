package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonClass;

@JsonClass(generateAdapter = true)
data class GaeClassroom(

  @Json(name = "topic_summary_dicts") val topic_summary_dicts: List<GaeClassroomTopicSummarytDict>?

)

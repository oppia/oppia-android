package org.oppia.app.backend.model;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonClass;

@JsonClass(generateAdapter = true)
data class TopicIndexHandler(

@Json(name = "topic_summary_dicts") val topic_summary_dicts: List<TopicIndexModel>?

)

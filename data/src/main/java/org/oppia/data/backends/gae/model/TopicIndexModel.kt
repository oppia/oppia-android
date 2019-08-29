package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonClass;
import org.oppia.app.backend.model.TopicSummarytDicts

@JsonClass(generateAdapter = true)
data class TopicIndexModel(

  @Json(name = "topic_summary_dicts") val topic_summary_dicts: List<TopicSummarytDicts>?

)

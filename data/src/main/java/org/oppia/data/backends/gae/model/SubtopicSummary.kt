package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubtopicSummary(

  @Json(name = "id") val id: String,
  @Json(name = "title") val title: String,
  @Json(name = "skill_ids") val skillIds: List<String>

)

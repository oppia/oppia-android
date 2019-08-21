package org.oppia.data.backends.gae.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubtopicSummary(

  val id: String,
  val title: String,
  val skill_ids: List<String>

)
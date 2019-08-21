package org.oppia.data.backends.gae.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CanonicalStorySummary(

  val id: String,
  val title: String,
  val description: String

)
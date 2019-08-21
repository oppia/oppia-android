package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Voiceover(

  @Json(name = "file_size_bytes") val fileSizeBytes: Long,
  @Json(name = "needs_update") val isUpdatedNeeded: Boolean,
  @Json(name = "filename") val filename: String

)

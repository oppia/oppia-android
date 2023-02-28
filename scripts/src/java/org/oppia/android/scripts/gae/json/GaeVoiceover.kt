package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeVoiceover(
  @Json(name = "filename") val filename: String,
  @Json(name = "file_size_bytes") val fileSizeBytes: Int,
  @Json(name = "needs_update") val needsUpdate: Boolean,
  @Json(name = "duration_secs") val durationSecs: Float
)

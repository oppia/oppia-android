package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeRecordedVoiceovers(
  @Json(name = "voiceovers_mapping") val voiceoversMapping: Map<String, Map<String, GaeVoiceover>>
)

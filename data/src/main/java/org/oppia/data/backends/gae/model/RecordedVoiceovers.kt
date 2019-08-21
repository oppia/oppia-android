package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecordedVoiceovers(

  @Json(name = "voiceovers_mapping") val voiceoversMapping: Map<String, Map<String, Voiceover>>

)

package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for RecordedVoiceovers model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L967
 */
@JsonClass(generateAdapter = true)
data class GaeRecordedVoiceovers(

  @Json(name = "voiceovers_mapping") val voiceoversMapping: Map<String, Map<String, GaeVoiceover>>?

)

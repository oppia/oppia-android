package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for RecordedVoiceovers model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/state_domain.py#L1770
 */
@JsonClass(generateAdapter = true)
data class GaeRecordedVoiceovers(

  @Json(name = "voiceovers_mapping") val voiceoversMapping: Map<String, Map<String, GaeVoiceover>>?

)

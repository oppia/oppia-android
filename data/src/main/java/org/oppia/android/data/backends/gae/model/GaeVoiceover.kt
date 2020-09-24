package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Voiceover model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L651
 */
@JsonClass(generateAdapter = true)
data class GaeVoiceover(

  @Json(name = "file_size_bytes") val fileSizeBytes: Long?,
  @Json(name = "needs_update") val isUpdateNeeded: Boolean?,
  @Json(name = "filename") val filename: String?

)

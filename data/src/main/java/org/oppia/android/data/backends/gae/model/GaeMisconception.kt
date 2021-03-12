package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Misconception model
 */
@JsonClass(generateAdapter = true)
data class GaeMisconception(

  @Json(name = "skill_id") val skillId: String?,
  @Json(name = "misconception_id") val misconceptionId: String?,

)

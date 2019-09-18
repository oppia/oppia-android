package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class model for Questions List
 * https://github.com/oppia/oppia/blob/b33aa9/core/controllers/reader.py#L1008
 */
@JsonClass(generateAdapter = true)
data class GaeQuestionPlayer(

  @Json(name = "question_dicts") val questions: List<GaeQuestion>?

)

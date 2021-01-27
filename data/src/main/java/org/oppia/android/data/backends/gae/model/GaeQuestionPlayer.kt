package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class model for Questions List
 * @link https://github.com/oppia/oppia/blob/develop/core/controllers/reader.py#L220
 */
@JsonClass(generateAdapter = true)
data class GaeQuestionPlayer(

  @Json(name = "question_dicts") val questions: List<GaeQuestion>?

)

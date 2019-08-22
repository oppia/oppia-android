package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuestionPlayer(

  @Json(name = "question_dicts") val questions: List<Question>?

)

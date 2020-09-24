package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Question model
 * https://github.com/oppia/oppia/blob/b33aa9/core/domain/question_domain.py#L144
 */
@JsonClass(generateAdapter = true)
data class GaeQuestion(

  @Json(name = "id") val id: String?,
  @Json(name = "question_state_data") val state: GaeState?,
  @Json(name = "version") val version: Int?,
  @Json(name = "linked_skill_ids") val linkedSkillIds: List<String>?

)

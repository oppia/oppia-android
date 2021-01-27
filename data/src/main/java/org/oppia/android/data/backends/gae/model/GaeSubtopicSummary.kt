package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Subtopic summary model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/topic_domain.py#L337
 */
@JsonClass(generateAdapter = true)
data class GaeSubtopicSummary(

  @Json(name = "id") val subtopicId: String?,
  @Json(name = "title") val title: String?,
  @Json(name = "skill_ids") val skillIds: List<String?>?

)

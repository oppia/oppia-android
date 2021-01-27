package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for ConceptCard model
 * @link https://github.com/oppia/oppia/blob/develop/core/controllers/concept_card_viewer.py#L42
 */
@JsonClass(generateAdapter = true)
data class GaeConceptCard(

  @Json(name = "concept_card_dicts") val conceptCardDicts: List<GaeSkillContents>?

)

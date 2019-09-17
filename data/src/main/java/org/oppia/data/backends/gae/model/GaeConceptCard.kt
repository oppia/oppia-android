package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for ConceptCard model
 *
 */
@JsonClass(generateAdapter = true)
data class GaeConceptCard(

  @Json(name = "concept_card_dicts") val conceptCardDicts: List<GaeSkillContents>

)

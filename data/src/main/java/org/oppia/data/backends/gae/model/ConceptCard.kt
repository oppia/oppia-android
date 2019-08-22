package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConceptCard(

  @Json(name = "concept_card_dicts") val conceptCardDicts: List<SkillContents>?

)

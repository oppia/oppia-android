package org.oppia.data.backends.gae.api

import org.oppia.data.backends.gae.model.ConceptCard
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ConceptCardService {

  @GET("concept_card_handler/{comma_separated_skill_ids}")
  fun getSkillContents(@Query("comma_separated_skill_ids") skillIds: String): Call<ConceptCard>

}

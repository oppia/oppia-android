package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaeConceptCard
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/** Service that provides access to concept-card endpoints. */
interface ConceptCardService {

  @GET("concept_card_handler/{comma_separated_skill_ids}")
  fun getSkillContents(@Query("comma_separated_skill_ids") skillIds: String): Call<GaeConceptCard>
}

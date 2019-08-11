package org.oppia.data.backends.gae.api;

import org.oppia.data.backends.gae.model.ConceptCard;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ConceptCardService {

  @GET("concept_card_handler/{comma_separated_skill_ids}")
  Call<ConceptCard> getSkillContents(@Path("comma_separated_skill_ids") String skillIds);

}

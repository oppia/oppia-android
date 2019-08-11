package org.oppia.data.backends.gae.api;

import org.oppia.data.backends.gae.model.QuestionPlayer;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QuestionPlayerService {

  @GET("question_player_handler")
  Call<QuestionPlayer> getQuestionPlayerBySkillIds(@Query("skill_ids") String skillIds, @Query("question_count") int questionCount);

}

package org.oppia.data.backends.gae.api

import org.oppia.data.backends.gae.model.QuestionPlayer
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface QuestionPlayerService {

  @GET("question_player_handler")
  fun getQuestionPlayerBySkillIds(@Query("skill_ids") skillIds: String, @Query("question_count") questionCount: Int): Call<QuestionPlayer>

}

package org.oppia.android.data.gae.gae.api

import org.oppia.android.data.gae.gae.model.GaeQuestionPlayer
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/** Service that provides access to question-player endpoints. */
interface QuestionPlayerService {

  @GET("question_player_handler")
  fun getQuestionPlayerBySkillIds(
    @Query("skill_ids") skillIds: String,
    @Query("question_count") questionCount: Int
  ): Call<GaeQuestionPlayer>
}

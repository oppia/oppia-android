package org.oppia.android.testing.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.android.data.gae.gae.NetworkInterceptor
import org.oppia.android.data.gae.gae.NetworkSettings
import org.oppia.android.data.gae.gae.api.QuestionPlayerService
import org.oppia.android.data.gae.gae.model.GaeQuestionPlayer
import org.oppia.android.testing.ApiUtils
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock QuestionPlayerService with dummy data from [question_player.json]
 */
class MockQuestionPlayerService(private val delegate: BehaviorDelegate<QuestionPlayerService>) :
  QuestionPlayerService {
  override fun getQuestionPlayerBySkillIds(
    skillIds: String,
    questionCount: Int
  ): Call<GaeQuestionPlayer> {
    val questionPlayer = createMockGaeQuestionPlayer()
    return delegate.returningResponse(questionPlayer)
      .getQuestionPlayerBySkillIds(skillIds, questionCount)
  }

  /**
   * This function creates a mock GaeQuestionPlayer with data from dummy json.
   * @return GaeQuestionPlayer: GaeQuestionPlayer with mock data
   */
  private fun createMockGaeQuestionPlayer(): GaeQuestionPlayer {
    val networkInterceptor = NetworkInterceptor()
    var questionPlayerResponseWithXssiPrefix =
      NetworkSettings.XSSI_PREFIX + ApiUtils.getFakeJson("question_player.json")

    questionPlayerResponseWithXssiPrefix =
      networkInterceptor.removeXSSIPrefix(questionPlayerResponseWithXssiPrefix)

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeQuestionPlayer> = moshi.adapter(GaeQuestionPlayer::class.java)
    val mockGaeQuestionPlayer = adapter.fromJson(questionPlayerResponseWithXssiPrefix)

    return mockGaeQuestionPlayer!!
  }
}

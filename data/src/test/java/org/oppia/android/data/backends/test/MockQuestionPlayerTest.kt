package org.oppia.android.data.backends.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.api.MockQuestionPlayerService
import org.oppia.android.data.backends.gae.api.QuestionPlayerService
import org.oppia.android.testing.network.MockRetrofitHelper
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit

/**
 * Test for [QuestionPlayerService] retrofit instance using [MockQuestionPlayerService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockQuestionPlayerTest {
  private lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    mockRetrofit = MockRetrofitHelper().createMockRetrofit()
  }

  @Test
  fun testQuestionPlayerService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(QuestionPlayerService::class.java)
    val mockQuestionPlayerService = MockQuestionPlayerService(delegate)

    val skillIdList = ArrayList<String>()
    skillIdList.add("1")
    skillIdList.add("2")
    skillIdList.add("3")
    val skillIds = skillIdList.joinToString(separator = ", ")
    val questionPlayer = mockQuestionPlayerService.getQuestionPlayerBySkillIds(skillIds, 10)
    val questionPlayerResponse = questionPlayer.execute()

    assertThat(questionPlayerResponse.isSuccessful).isTrue()
    assertThat(questionPlayerResponse.body()!!.questions!!.size).isEqualTo(1)
  }
}

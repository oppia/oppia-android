package org.oppia.android.data.backends.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.api.MockQuestionPlayerService
import org.oppia.android.data.backends.gae.NetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import org.oppia.android.data.backends.gae.api.QuestionPlayerService
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

/**
 * Test for [QuestionPlayerService] retrofit instance using [MockQuestionPlayerService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockQuestionPlayerTest {
  private lateinit var mockRetrofit: MockRetrofit
  private lateinit var retrofit: Retrofit

  @Before
  fun setUp() {
    val client = OkHttpClient.Builder()
    client.addInterceptor(NetworkInterceptor())

    retrofit = retrofit2.Retrofit.Builder()
      .baseUrl(NetworkSettings.getBaseUrl())
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client.build())
      .build()

    val behavior = NetworkBehavior.create()
    mockRetrofit = MockRetrofit.Builder(retrofit)
      .networkBehavior(behavior)
      .build()
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

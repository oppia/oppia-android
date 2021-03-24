package org.oppia.android.data.gae.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.network.MockTopicService
import org.oppia.android.data.gae.gae.NetworkInterceptor
import org.oppia.android.data.gae.gae.NetworkSettings
import org.oppia.android.data.gae.gae.api.TopicService
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

/**
 * Test for [TopicService] retrofit instance using [MockTopicService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TopicServiceTest {
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
  fun testTopicService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(TopicService::class.java)
    val mockTopicService = MockTopicService(delegate)

    val topic = mockTopicService.getTopicByName("Topic1")
    val topicResponse = topic.execute()

    assertThat(topicResponse.isSuccessful).isTrue()
    assertThat(topicResponse.body()!!.topicName).isEqualTo("Topic1")
  }
}

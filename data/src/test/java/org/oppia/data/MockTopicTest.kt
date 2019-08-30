package org.oppia.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.data.backends.gae.NetworkInterceptor
import org.oppia.data.backends.gae.api.TopicService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

/**
 * Test for [TopicService] retrofit instance using [MockTopicService]
 */
@RunWith(AndroidJUnit4::class)
class MockTopicTest {
  private var mockRetrofit: MockRetrofit? = null
  private var retrofit: Retrofit? = null

  @Before
  @Throws(Exception::class)
  fun setUp() {
    val networkInterceptor = NetworkInterceptor()

    val client = OkHttpClient.Builder()
    client.addInterceptor(networkInterceptor)

    retrofit = Retrofit.Builder()
      .baseUrl("https://www.testoppia.com")
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client.build())
      .build()

    val behavior = NetworkBehavior.create()
    mockRetrofit = MockRetrofit.Builder(retrofit!!)
      .networkBehavior(behavior)
      .build()
  }

  @Test
  @Throws(Exception::class)
  fun testMockTopicService_withTopicName_success() {
    val delegate = mockRetrofit!!.create(TopicService::class.java)
    val mockTopicService = MockTopicService(delegate)

    val topic = mockTopicService.getTopicByName("Test Topic")
    val topicResponse = topic.execute()

    assertThat(topicResponse.isSuccessful).isTrue()
    assertThat("Test Topic").isEqualTo(topicResponse.body()!!.topicName)
  }
}

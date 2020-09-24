package org.oppia.data.backends.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.data.backends.api.MockExplorationService
import org.oppia.data.backends.gae.NetworkInterceptor
import org.oppia.data.backends.gae.NetworkSettings
import org.oppia.data.backends.gae.api.ExplorationService
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

/**
 * Test for [ExplorationService] retrofit instance using [MockExplorationService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockExplorationTest {
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
  fun testExplorationService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(ExplorationService::class.java)
    val mockExplorationService = MockExplorationService(delegate)

    val explorationContainer = mockExplorationService.getExplorationById("4")
    val explorationContainerResponse = explorationContainer.execute()

    assertThat(explorationContainerResponse.isSuccessful).isTrue()
    assertThat(explorationContainerResponse.body()!!.explorationId).isEqualTo("4")
  }
}

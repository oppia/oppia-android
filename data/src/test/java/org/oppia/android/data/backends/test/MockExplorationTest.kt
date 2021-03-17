package org.oppia.android.data.backends.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.api.MockExplorationService
import org.oppia.android.data.backends.gae.api.ExplorationService
import org.oppia.android.testing.network.MockRetrofitHelper
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit

/**
 * Test for [ExplorationService] retrofit instance using [MockExplorationService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockExplorationTest {
  private lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    mockRetrofit = MockRetrofitHelper().createMockRetrofit()
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

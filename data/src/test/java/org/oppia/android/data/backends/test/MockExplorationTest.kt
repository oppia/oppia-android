package org.oppia.android.data.backends.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.api.MockExplorationService
import org.oppia.android.data.backends.gae.api.ExplorationService
import org.oppia.android.testing.network.RetrofitTestModule
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for [ExplorationService] retrofit instance using [MockExplorationService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockExplorationTest {

  @Inject
  lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
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

  private fun setUpTestApplicationComponent() {
    DaggerMockExplorationTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [RetrofitTestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: MockExplorationTest)
  }
}

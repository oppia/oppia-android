package org.oppia.android.testing.platformparameter

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.data.backends.gae.model.GaePlatformParameter
import org.oppia.android.data.backends.gae.model.GaePlatformParameters
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MockPlatformParameterService]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class MockPlatformParameterServiceTest {

  @Inject
  lateinit var mockPlatformParameterService: MockPlatformParameterService

  private val expectedNetworkResponseBody by lazy {
    arrayListOf<GaePlatformParameter>(
      GaePlatformParameter(TEST_STRING_PARAM_NAME, TEST_STRING_PARAM_VALUE),
      GaePlatformParameter(TEST_INTEGER_PARAM_NAME, TEST_INTEGER_PARAM_VALUE),
      GaePlatformParameter(TEST_BOOLEAN_PARAM_NAME, TEST_BOOLEAN_PARAM_VALUE)
    )
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun mockServiceTest_getPlatformParameter_verifyTheResponse() {
    val response = mockPlatformParameterService.getPlatformParametersByVersion("1").execute()
    assertThat(response.isSuccessful).isTrue()

    val responseBody = response.body()
    assertThat(responseBody).isNotNull()
    verifyResponseBody(responseBody!!)
  }

  // Checks for the individual platform parameters in the response body and compares them with the
  // expected network response body
  private fun verifyResponseBody(responseBody: GaePlatformParameters) {
    val gaePlatformParameterList = responseBody.platformParameters!!
    assertThat(gaePlatformParameterList.size).isEqualTo(expectedNetworkResponseBody.size)
    for (parameter in expectedNetworkResponseBody) {
      assertThat(gaePlatformParameterList).contains(parameter)
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerMockPlatformParameterServiceTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class, NetworkModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(mockPlatformParameterServiceTest: MockPlatformParameterServiceTest)
  }
}

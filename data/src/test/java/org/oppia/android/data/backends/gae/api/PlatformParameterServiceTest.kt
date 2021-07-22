package org.oppia.android.data.backends.gae.api

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
import org.oppia.android.testing.network.MockPlatformParameterService
import org.oppia.android.testing.network.RetrofitTestModule
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_SERVER_VALUE
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Tests for [PlatformParameterService] retrofit instance using
 * [org.oppia.android.testing.network.MockPlatformParameterService].
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class PlatformParameterServiceTest {

  @field:[Inject MockPlatformParameterService]
  lateinit var mockPlatformParameterService: PlatformParameterService

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPlatformParameterService_getPlatformParameterUsingMockService_verifyTheResponse() {
    val response = mockPlatformParameterService.getPlatformParametersByVersion("1").execute()
    assertThat(response.isSuccessful).isTrue()

    val responseBody = response.body()
    assertThat(responseBody).isNotNull()
  }

  @Test
  fun testPlatformParameterService_getPlatformParameterUsingMockService_checkForStringParam() {
    val response = mockPlatformParameterService.getPlatformParametersByVersion("1").execute()
    val responseBody = response.body()!!
    assertThat(responseBody[TEST_STRING_PARAM_NAME]).isEqualTo(TEST_STRING_PARAM_SERVER_VALUE)
  }

  @Test
  fun testPlatformParameterService_getPlatformParameterUsingMockService_checkForIntegerParam() {
    val response = mockPlatformParameterService.getPlatformParametersByVersion("1").execute()
    val responseBody = response.body()!!
    assertThat(responseBody[TEST_INTEGER_PARAM_NAME]).isEqualTo(TEST_INTEGER_PARAM_SERVER_VALUE)
  }

  @Test
  fun testPlatformParameterService_getPlatformParameterUsingMockService_checkForBooleanParam() {
    val response = mockPlatformParameterService.getPlatformParametersByVersion("1").execute()
    val responseBody = response.body()!!
    assertThat(responseBody[TEST_BOOLEAN_PARAM_NAME]).isEqualTo(TEST_BOOLEAN_PARAM_SERVER_VALUE)
  }

  private fun setUpTestApplicationComponent() {
    DaggerPlatformParameterServiceTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // Annotation for MockPlatformParameterService
  @Qualifier
  annotation class MockPlatformParameterService

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @Provides
    @Singleton
    @MockPlatformParameterService
    fun provideMockPlatformParameterService(mockRetrofit: MockRetrofit): PlatformParameterService {
      val delegate = mockRetrofit.create(PlatformParameterService::class.java)
      return MockPlatformParameterService(delegate)
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, NetworkModule::class, RetrofitTestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(platformParameterServiceTest: PlatformParameterServiceTest)
  }
}

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
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.NetworkConfigModule
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
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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

  private lateinit var mockWebServer: MockWebServer

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPlatformParameterService_setUpNetworkRequestToGetPlatformParameter_verifyTheRequest() {
    val platformParameterService = setUpInterceptedPlatformParameterService()
    mockWebServer.enqueue(MockResponse().setBody("{}"))

    platformParameterService.getPlatformParametersByVersion("1").execute()
    val interceptedRequest = mockWebServer.takeRequest()

    val appVersion = interceptedRequest.requestUrl?.queryParameter("app_version")
    assertThat(appVersion).isEqualTo("1")

    val platformType = interceptedRequest.requestUrl?.queryParameter("platform_type")
    assertThat(platformType).isEqualTo("Android")
  }

  @Test
  fun testPlatformParameterService_getPlatformParameterUsingMockService_verifySuccessfulResponse() {
    val response = mockPlatformParameterService
      .getPlatformParametersByVersion(version = "1")
      .execute()
    assertThat(response.isSuccessful).isTrue()

    val responseBody = response.body()
    assertThat(responseBody).isNotNull()
  }

  @Test
  fun testPlatformParameterService_getPlatformParameterUsingMockService_checkForStringParam() {
    val response = mockPlatformParameterService
      .getPlatformParametersByVersion(version = "1")
      .execute()
    val responseBody = response.body()!!
    assertThat(responseBody).containsEntry(TEST_STRING_PARAM_NAME, TEST_STRING_PARAM_SERVER_VALUE)
  }

  @Test
  fun testPlatformParameterService_getPlatformParameterUsingMockService_checkForIntegerParam() {
    val response = mockPlatformParameterService
      .getPlatformParametersByVersion(version = "1")
      .execute()
    val responseBody = response.body()!!
    assertThat(responseBody).containsEntry(TEST_INTEGER_PARAM_NAME, TEST_INTEGER_PARAM_SERVER_VALUE)
  }

  @Test
  fun testPlatformParameterService_getPlatformParameterUsingMockService_checkForBooleanParam() {
    val response = mockPlatformParameterService
      .getPlatformParametersByVersion(version = "1")
      .execute()
    val responseBody = response.body()!!
    assertThat(responseBody).containsEntry(TEST_BOOLEAN_PARAM_NAME, TEST_BOOLEAN_PARAM_SERVER_VALUE)
  }

  private fun setUpTestApplicationComponent() {
    DaggerPlatformParameterServiceTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun setUpInterceptedPlatformParameterService(): PlatformParameterService {
    mockWebServer = MockWebServer()
    val client = OkHttpClient.Builder().build()

    // Use retrofit with the MockWebServer here instead of MockRetrofit so that we can verify that
    // the full network request properly executes. MockRetrofit and MockWebServer perform the same
    // request mocking in different ways and we want to verify the full request is executed here.
    // See https://github.com/square/retrofit/issues/2340#issuecomment-302856504 for more context.
    val retrofit = Retrofit.Builder()
      .baseUrl(mockWebServer.url("/"))
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client)
      .build()

    return retrofit.create(PlatformParameterService::class.java)
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
      TestModule::class, NetworkModule::class,
      RetrofitTestModule::class, NetworkConfigModule::class
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

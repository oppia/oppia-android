package org.oppia.android.data.backends.gae

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.BackgroundTestDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatcher
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [RemoteAuthNetworkInterceptor]. */
@RunWith(AndroidJUnit4::class)
@Config(application = RemoteAuthNetworkInterceptorTest.TestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RemoteAuthNetworkInterceptorTest {
  @Inject lateinit var context: Context
  @Inject lateinit var remoteAuthNetworkInterceptor: RemoteAuthNetworkInterceptor
  @Inject lateinit var moshi: Moshi
  @Inject lateinit var mockWebServer: MockWebServer
  @field:[Inject BackgroundTestDispatcher]
  lateinit var testCoroutineDispatcher: TestCoroutineDispatcher

  private lateinit var retrofit: Retrofit
  private lateinit var client: OkHttpClient
  private lateinit var platformParameterService: PlatformParameterService

  private val testVersionName = "1.0"

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpRetrofit()
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testNetworkInterceptor_withoutAnyHeaders_addsCorrectHeaders() {
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    val call = platformParameterService.getPlatformParametersByVersion(testVersionName)
    val serviceRequest = call.request()
    assertThat(serviceRequest.header("api_key")).isNull()
    assertThat(serviceRequest.header("app_package_name")).isNull()
    assertThat(serviceRequest.header("app_version_name")).isNull()
    assertThat(serviceRequest.header("app_version_code")).isNull()

    call.execute()
    val interceptedRequest = mockWebServer.takeRequest(
      timeout = testCoroutineDispatcher.DEFAULT_TIMEOUT_SECONDS,
      unit = testCoroutineDispatcher.DEFAULT_TIMEOUT_UNIT
    )

    verifyRequestHeaders(interceptedRequest?.headers)
  }

  @Test
  fun testNetworkInterceptor_withIncorrectHeaders_setsCorrectHeaders() {
    val request = Request.Builder()
      .url(mockWebServer.url("/"))
      .addHeader("api_key", "wrong_api_key")
      .addHeader("app_package_name", "wrong_package_name")
      .addHeader("app_version_name", "wrong_version_name")
      .addHeader("app_version_code", "wrong_version_code")
      .build()
    assertThat(request.header("api_key")).isEqualTo("wrong_api_key")
    assertThat(request.header("app_package_name")).isEqualTo("wrong_package_name")
    assertThat(request.header("app_version_name")).isEqualTo("wrong_version_name")
    assertThat(request.header("app_version_code")).isEqualTo("wrong_version_code")

    mockWebServer.enqueue(MockResponse().setBody("{}"))
    client.newCall(request).execute()
    val interceptedRequest = mockWebServer.takeRequest(
      timeout = testCoroutineDispatcher.DEFAULT_TIMEOUT_SECONDS,
      unit = testCoroutineDispatcher.DEFAULT_TIMEOUT_UNIT
    )

    verifyRequestHeaders(interceptedRequest?.headers)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpRetrofit() {
    client = OkHttpClient.Builder()
      .addInterceptor(remoteAuthNetworkInterceptor)
      .build()
    retrofit = Retrofit.Builder()
      .baseUrl(mockWebServer.url("/"))
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .client(client)
      .build()
    platformParameterService = retrofit.create(PlatformParameterService::class.java)
  }

  private fun verifyRequestHeaders(headers: Headers?) {
    assertThat(headers).isNotNull()
    assertThat(headers?.get("api_key")).isEqualTo("test_api_key")
    assertThat(headers?.get("app_package_name")).isEqualTo(context.packageName)
    assertThat(headers?.get("app_version_name")).isEqualTo("oppia-android-test-0123456789")
    assertThat(headers?.get("app_version_code")).isEqualTo("1")
  }

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
  @Component(
    modules = [
      RobolectricModule::class, RetrofitModule::class, RetrofitServiceModule::class,
      TestModule::class, TestLogReportingModule::class, TestDispatcherModule::class,
      NetworkConfigTestModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(remoteAuthNetworkInterceptorTest: RemoteAuthNetworkInterceptorTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerRemoteAuthNetworkInterceptorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(remoteAuthNetworkInterceptorTest: RemoteAuthNetworkInterceptorTest) {
      component.inject(remoteAuthNetworkInterceptorTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}

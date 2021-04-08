package org.oppia.android.data.backends.gae

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.MoreExecutors
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.Dispatcher
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.api.TopicService
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.network.MockTopicService
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [RemoteAuthNetworkInterceptor] */
@RunWith(AndroidJUnit4::class)
@Config(application = RemoteAuthNetworkInterceptorTest.TestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RemoteAuthNetworkInterceptorTest {

  @Inject
  lateinit var remoteAuthNetworkInterceptor: RemoteAuthNetworkInterceptor

  @Inject
  lateinit var context: Context

  private lateinit var mockRetrofit: MockRetrofit

  private lateinit var retrofit: Retrofit

  private val testVersionName = "1.0"

  private val testVersionCode = 1

  private val topicName = "Topic1"

  private val mockWebServer = MockWebServer()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpApplicationForContext()
  }

  private fun setUpApplicationForContext() {
    val packageManager = Shadows.shadowOf(context.packageManager)
    val applicationInfo =
      ApplicationInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .build()
    val packageInfo =
      PackageInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setApplicationInfo(applicationInfo)
        .build()
    packageInfo.versionName = testVersionName
    packageInfo.versionCode = testVersionCode
    packageManager.installPackage(packageInfo)
  }

  @Test
  fun testNetworkInterceptor_withoutHeaders_addsCorrectHeaders() {
    mockWebServer.enqueue(MockResponse().setBody("{}"))
    val request = Request.Builder()
      .url(mockWebServer.url(NetworkSettings.getBaseUrl() + "/topic_data_handler/$topicName"))
      .get()
      .build()
    assertThat(request.header("api_key")).isNull()
    assertThat(request.header("app_package_name")).isNull()
    assertThat(request.header("app_version_name")).isNull()
    assertThat(request.header("app_version_code")).isNull()

    val client = OkHttpClient.Builder()
      .dispatcher(Dispatcher(MoreExecutors.newDirectExecutorService()))
      .addInterceptor(remoteAuthNetworkInterceptor)
      .build()
    client.newCall(request).execute()

    val interceptedRequest = mockWebServer.takeRequest(timeout = 8, unit = TimeUnit.SECONDS)
    verifyRequestHeaders(interceptedRequest!!.headers)
  }

  @Test
  fun testNetworkInterceptor_withHeaders_setsCorrectHeaders() {
    val delegate = mockRetrofit.create(TopicService::class.java)
    val mockTopicService = MockTopicService(delegate)

    val serviceRequest = mockTopicService.getTopicByName(topicName).request()
    val request = serviceRequest.newBuilder()
      .addHeader("api_key", "wrong_api_key")
      .addHeader("app_package_name", "wrong_package_name")
      .addHeader("app_version_name", "wrong_version_name")
      .addHeader("app_version_code", "wrong_version_code")
      .build()

    val newRequest = remoteAuthNetworkInterceptor.addAuthHeaders(request)
    verifyRequestHeaders(newRequest.headers)
  }

  private fun verifyRequestHeaders(headers: Headers) {
    assertThat(headers.get("api_key")).isEqualTo("test_api_key")
    assertThat(headers.get("app_package_name")).isEqualTo(context.packageName)
    assertThat(headers.get("app_version_name")).isEqualTo("1.0")
    assertThat(headers.get("app_version_code")).isEqualTo("1")
  }

  private fun setUpMockRetrofit() {
    val client = OkHttpClient.Builder()
      .dispatcher(Dispatcher(MoreExecutors.newDirectExecutorService()))
    client.addInterceptor(remoteAuthNetworkInterceptor)

    retrofit = retrofit2.Retrofit.Builder()
      .baseUrl(mockWebServer.url(NetworkSettings.getBaseUrl() + "/"))
      .addConverterFactory(MoshiConverterFactory.create())
      .callbackExecutor(MoreExecutors.directExecutor())
      .client(client.build())
      .build()

    val behavior = NetworkBehavior.create()
    mockRetrofit = MockRetrofit.Builder(retrofit)
      .networkBehavior(behavior)
      .backgroundExecutor(MoreExecutors.newDirectExecutorService())
      .build()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Qualifier
  annotation class OppiaRetrofit

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestNetworkModule {
    @Provides
    @Singleton
    fun provideMockTopicService(@OppiaRetrofit retrofit: Retrofit): MockTopicService {
      return retrofit.create(MockTopicService::class.java)
    }

    @Provides
    @Singleton
    @NetworkApiKey
    fun provideNetworkApiKey(): String = "test_api_key"
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
      RobolectricModule::class, TestNetworkModule::class, TestModule::class,
      TestLogReportingModule::class, TestDispatcherModule::class
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

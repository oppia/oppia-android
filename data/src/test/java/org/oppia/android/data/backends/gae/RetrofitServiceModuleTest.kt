package org.oppia.android.data.backends.gae

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.BackgroundTestDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Tests for [RetrofitServiceModule]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = RetrofitServiceModuleTest.TestApplication::class)
class RetrofitServiceModuleTest {
  @field:[Inject OppiaRetrofit] lateinit var retrofit: Retrofit
  @Inject lateinit var context: Context
  @Inject lateinit var feedbackReportingServiceProvider: Provider<FeedbackReportingService>
  @Inject lateinit var platformParameterServiceProvider: Provider<PlatformParameterService>
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @field:[Inject BackgroundTestDispatcher]
  lateinit var backgroundTestDispatcher: TestCoroutineDispatcher

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    setUpApplicationForContext()
  }

  @Test
  fun testInjectedFeedbackReportingService_secondInjection_returnsSingletonInstance() {
    val firstInjection = feedbackReportingServiceProvider.get()
    val secondInjection = feedbackReportingServiceProvider.get()

    // Multiple injections should yield the same instance due to it being a singleton.
    assertThat(firstInjection).isEqualTo(secondInjection)
  }

  @Test
  fun testInjectedPlatformParameterService_secondInjection_returnsSingletonInstance() {
    val firstInjection = platformParameterServiceProvider.get()
    val secondInjection = platformParameterServiceProvider.get()

    // Multiple injections should yield the same instance due to it being a singleton.
    assertThat(firstInjection).isEqualTo(secondInjection)
  }

  private fun getTestApplication() = ApplicationProvider.getApplicationContext<TestApplication>()

  private fun setUpTestApplicationComponent() {
    getTestApplication().inject(this)
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
    packageInfo.versionName = TEST_APP_VERSION_NAME
    @Suppress("DEPRECATION") // versionCode is needed to test production code.
    packageInfo.versionCode = TEST_APP_VERSION_CODE
    packageManager.installPackage(packageInfo)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @BaseUrl
    fun provideNetworkBaseUrl(mockWebServer: MockWebServer): String =
      mockWebServer.url("/").toUrl().toString()

    @Provides
    @XssiPrefix
    fun provideXssiPrefix() = XSSI_PREFIX
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, RetrofitModule::class, RetrofitServiceModule::class,
      TestDispatcherModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(networkModuleTest: RetrofitServiceModuleTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerRetrofitServiceModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(networkModuleTest: RetrofitServiceModuleTest) {
      component.inject(networkModuleTest)
    }
  }

  interface TestService {
    @GET("test_path/test_object_handler")
    // TODO(#76): Update return payload for handling storage failures once retry policy is defined.
    fun fetchTestObject(): Call<TestMoshiObject>

    @GET("test_path/test_nothing_handler")
    fun fetchNothing(): Call<Any>
  }

  @JsonClass(generateAdapter = true)
  data class TestMoshiObject(
    @Json(name = "field1") val field1: String,
    @Json(name = "field2") val field2: Int
  )

  private companion object {
    private const val XSSI_PREFIX = ")]}'"
    private const val TEST_APP_VERSION_NAME = "oppia-android-test-0123456789"
    private const val TEST_APP_VERSION_CODE = 1
  }
}

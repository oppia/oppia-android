package org.oppia.android.data.backends.gae

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.base.Optional
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.api.ClassroomService
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.data.backends.gae.api.TopicService
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit

/** Tests for [NetworkModule]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = NetworkModuleTest.TestApplication::class)
class NetworkModuleTest {
  @field:[Inject NetworkApiKey]
  lateinit var networkApiKey: String

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
  fun testRetrofitInstance_lollipop_isProvided() {
    assertThat(getTestApplication().getRetrofit()).isPresent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.KITKAT])
  fun testRetrofitInstance_kitkat_isEmpty() {
    assertThat(getTestApplication().getRetrofit()).isAbsent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
  fun testTopicService_lollipop_isProvided() {
    assertThat(getTestApplication().getTopicService()).isPresent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.KITKAT])
  fun testTopicService_kitkat_isEmpty() {
    assertThat(getTestApplication().getTopicService()).isAbsent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
  fun testClassroomService_lollipop_isProvided() {
    assertThat(getTestApplication().getClassroomService()).isPresent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.KITKAT])
  fun testClassroomService_kitkat_isEmpty() {
    assertThat(getTestApplication().getClassroomService()).isAbsent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
  fun testFeedbackReportingService_lollipop_isProvided() {
    assertThat(getTestApplication().getFeedbackReportingService()).isPresent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.KITKAT])
  fun testFeedbackReportingService_kitkat_isEmpty() {
    assertThat(getTestApplication().getFeedbackReportingService()).isAbsent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
  fun testPlatformParameterService_lollipop_isProvided() {
    assertThat(getTestApplication().getPlatformParameterService()).isPresent()
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.KITKAT])
  fun testPlatformParameterService_kitkat_isEmpty() {
    assertThat(getTestApplication().getPlatformParameterService()).isAbsent()
  }

  @Test
  fun testNetworkApiKey_isEmpty() {
    // The network API key is empty by default on developer builds.
    assertThat(networkApiKey).isEmpty()
  }

  private fun getTestApplication() = ApplicationProvider.getApplicationContext<TestApplication>()

  private fun setUpTestApplicationComponent() {
    getTestApplication().inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, NetworkModule::class, NetworkConfigProdModule::class
    ]
  )

  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(networkModuleTest: NetworkModuleTest)
    @OppiaRetrofit fun getRetrofit(): Optional<Retrofit>
    fun getTopicService(): Optional<TopicService>
    fun getClassroomService(): Optional<ClassroomService>
    fun getFeedbackReportingService(): Optional<FeedbackReportingService>
    fun getPlatformParameterService(): Optional<PlatformParameterService>
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerNetworkModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(networkModuleTest: NetworkModuleTest) {
      component.inject(networkModuleTest)
    }

    fun getRetrofit() = component.getRetrofit()
    fun getTopicService() = component.getTopicService()
    fun getClassroomService() = component.getClassroomService()
    fun getFeedbackReportingService() = component.getFeedbackReportingService()
    fun getPlatformParameterService() = component.getPlatformParameterService()
  }
}

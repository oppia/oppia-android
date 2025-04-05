package org.oppia.android.data.backends.gae

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
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.BackgroundTestDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
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

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, RetrofitModule::class, RetrofitServiceModule::class,
      TestDispatcherModule::class, RobolectricModule::class, NetworkConfigTestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: RetrofitServiceModuleTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerRetrofitServiceModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: RetrofitServiceModuleTest) {
      component.inject(test)
    }
  }
}

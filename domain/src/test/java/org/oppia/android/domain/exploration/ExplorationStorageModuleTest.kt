package org.oppia.android.domain.exploration

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ExplorationStorageModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationStorageModuleTest.TestApplication::class)
class ExplorationStorageModuleTest {
  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @field:[Inject JvmField ExplorationStorageDatabaseSize] var databaseSize: Int = Int.MIN_VALUE
  @Inject lateinit var explorationRetriever: ExplorationRetriever

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testExplorationStorageDataSizeInjection_isEqualToTwoMebibytes() {
    assertThat(databaseSize).isEqualTo(2 * 1024 * 1024)
  }

  @Test
  fun testExplorationRetrieverInjection_retrieverIsProductionImplementation() {
    assertThat(explorationRetriever).isInstanceOf(ExplorationRetrieverImpl::class.java)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE

    @CacheAssetsLocally
    @Provides
    fun provideCacheAssetsLocally(): Boolean = false

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class,
      ExplorationStorageModule::class, TestDispatcherModule::class, RobolectricModule::class,
      LogStorageModule::class, NetworkConnectionUtilDebugModule::class, AssetModule::class,
      LocaleProdModule::class, FakeOppiaClockModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(explorationStorageModuleTest: ExplorationStorageModuleTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationStorageModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationStorageModuleTest: ExplorationStorageModuleTest) {
      component.inject(explorationStorageModuleTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}

package org.oppia.android.testing.threading

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.BackgroundExecutor
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.ScheduledExecutorService
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TestDispatcherModule] bindings. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TestDispatcherModuleTest.TestApplication::class)
class TestDispatcherModuleTest {
  @Inject
  @field:BackgroundExecutor
  lateinit var backgroundExecutor: ScheduledExecutorService

  @Inject
  @field:GlideTestExecutor
  lateinit var glideTestExecutor: ScheduledExecutorService

  @Inject
  @field:BackgroundDispatcher
  lateinit var backgroundDispatcher: CoroutineDispatcher

  @Inject
  lateinit var monitoredTaskCoordinators: Set<@JvmSuppressWildcards MonitoredTaskCoordinator>

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    TestModule.isOnRobolectric = null
  }

  @Test
  fun testBackgroundExecutor_onRobolectric_isBlockingScheduledExecutorService() {
    arrangeOnRobolectric()

    setUpTestApplicationComponent()

    // Verify that the background executor is replaced by the correct test executor.
    assertThat(backgroundExecutor).isInstanceOf(BlockingScheduledExecutorService::class.java)
  }

  @Test
  fun testBackgroundExecutor_onEspresso_isRealTimeScheduledExecutorService() {
    arrangeOnEspresso()

    setUpTestApplicationComponent()

    // Verify that the background executor is replaced by the correct test executor.
    assertThat(backgroundExecutor).isInstanceOf(RealTimeScheduledExecutorService::class.java)
  }

  @Test
  fun testGlideTestExecutor_onRobolectric_isBlockingScheduledExecutorService() {
    arrangeOnRobolectric()

    setUpTestApplicationComponent()

    // Verify that the Glide test-only executor is replaced by the correct test executor.
    assertThat(glideTestExecutor).isInstanceOf(BlockingScheduledExecutorService::class.java)
  }

  @Test
  fun testGlideTestExecutor_onEspresso_isRealTimeScheduledExecutorService() {
    arrangeOnEspresso()

    setUpTestApplicationComponent()

    // Verify that the Glide test-only executor is replaced by the correct test executor.
    assertThat(glideTestExecutor).isInstanceOf(RealTimeScheduledExecutorService::class.java)
  }

  @Test
  fun testBackgroundDispatcher_onRobolectric_isCoroutineExecutorTiedToBackgroundExecutor() {
    arrangeOnRobolectric()

    setUpTestApplicationComponent()

    // Verify that the background dispatcher is replaced by the correct test dispatcher.
    val executor = (backgroundDispatcher as? ExecutorCoroutineDispatcher)?.executor
    assertThat(backgroundDispatcher).isInstanceOf(ExecutorCoroutineDispatcher::class.java)
    assertThat(executor).isEqualTo(backgroundExecutor)
  }

  @Test
  fun testBackgroundDispatcher_onEspresso_isCoroutineExecutorTiedToBackgroundExecutor() {
    arrangeOnEspresso()

    setUpTestApplicationComponent()

    // Verify that the background dispatcher is replaced by the correct test dispatcher.
    val executor = (backgroundDispatcher as? ExecutorCoroutineDispatcher)?.executor
    assertThat(backgroundDispatcher).isInstanceOf(ExecutorCoroutineDispatcher::class.java)
    assertThat(executor).isEqualTo(backgroundExecutor)
  }

  @Test
  fun testMonitoredTaskCoordinatorSet_onRobolectric_containsAppExecutorsAndOneForRobolectric() {
    arrangeOnRobolectric()

    setUpTestApplicationComponent()

    // The executor set includes an extra Robolectric element in Robolectric environments.
    assertThat(monitoredTaskCoordinators).hasSize(2)
    assertThat(monitoredTaskCoordinators).contains(backgroundExecutor)
    assertThat(monitoredTaskCoordinators).contains(glideTestExecutor)
    assertThat(
      monitoredTaskCoordinators.any {
        it is MonitoredRobolectricUiTaskCoordinator
      }
    ).isTrue()
  }

  @Test
  fun testMonitoredTaskCoordinatorSet_onEspresso_containsOnlyAppExecutors() {
    arrangeOnEspresso()

    setUpTestApplicationComponent()

    // The executor set includes only the custom app task executors.
    assertThat(monitoredTaskCoordinators).hasSize(1)
    assertThat(monitoredTaskCoordinators).contains(backgroundExecutor)
    assertThat(monitoredTaskCoordinators).contains(glideTestExecutor)
    assertThat(
      monitoredTaskCoordinators.any {
        it is MonitoredRobolectricUiTaskCoordinator
      }
    ).isFalse()
  }

  @Test
  fun testTestCoroutineDispatchers_onRobolectric_isRobolectricImplementation() {
    arrangeOnRobolectric()

    setUpTestApplicationComponent()

    assertThat(testCoroutineDispatchers)
      .isInstanceOf(TestCoroutineDispatchersRobolectricImpl::class.java)
  }

  @Test
  fun testTestCoroutineDispatchers_onEspresso_isEspressoImplementation() {
    arrangeOnEspresso()

    setUpTestApplicationComponent()

    assertThat(testCoroutineDispatchers)
      .isInstanceOf(TestCoroutineDispatchersEspressoImpl::class.java)
  }

  private fun arrangeOnRobolectric() {
    TestModule.isOnRobolectric = { true }
  }

  private fun arrangeOnEspresso() {
    TestModule.isOnRobolectric = { false }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    internal companion object {
      var isOnRobolectric: (() -> Boolean)? = null
    }

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

    @Provides
    @IsOnRobolectric
    fun provideIsOnRobolectric(): Boolean =
      checkNotNull(isOnRobolectric) { "Test is not correctly initialized" }()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class,
      TestModule::class,
      TestLogReportingModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: TestDispatcherModuleTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTestDispatcherModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: TestDispatcherModuleTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}

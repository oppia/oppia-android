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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/**
 * Tests for [TestCoroutineDispatcherEspressoImpl].
 *
 * Note that this is testing the Espresso implementation of the test coroutine dispatcher using
 * Robolectric which means certain time constructs need to be used to try and control threading
 * since the tests can't rely on synchronization barriers (like the dispatcher itself).
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TestCoroutineDispatcherEspressoImplTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TestCoroutineDispatcherEspressoImplTest : TestCoroutineDispatcherTestBase(
  shortTaskDelayMillis = 5L,
  longTaskDelayMillis = 15000L,
  longTaskDelayDeltaCheckMillis = 1000L
) {
  @Before
  override fun setUp() {
    setUpTestApplicationComponent()
    verifyDispatcherImplementation<TestCoroutineDispatcherEspressoImpl>()
  }

  override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

  override fun advanceTimeBy(timeMillis: Long) {
    // While an antipattern, the tests are configured per the task delay passed in the constructor
    // to use small delays so that different test scenarios can be tested in a real thread
    // environment.
    Thread.sleep(timeMillis)
  }

  @Suppress("ControlFlowWithEmptyBody")
  override fun stabilizeAfterDispatcherFlush() {
    // Spin the test thread until the dispatcher has finished. Note that '{}' is used instead of a
    // semicolon since ktlint may incorrectly remove semicolons in valid cases. See #3052 for
    // context.
    while (backgroundTestDispatcher.hasPendingTasks()) {}
  }

  override fun ensureFutureTasksAreScheduled() {
    val mockRunnable = mock(Runnable::class.java)
    scheduleFutureTask(delayMs = 1L, mockRunnable)

    // Wait up to 500ms for the delayed runnable to be called. This should be sufficiently less than
    // the long time delay for tests to complete.
    verify(mockRunnable, timeout(/* millis= */ 500L).atLeastOnce()).run()

    // Wait a bit longer in case the quick execution introduces an ordering issue.
    advanceTimeBy(10L)
  }

  // Note that this test intentionally differs from Robolectric: the Espresso implementation chooses
  // to consider a future task as in-progress even during the delay since it needs to communicate
  // back to Espresso (via an IdlingResource) that a task needs to be run. This intentionally
  // differs from the Robolectric version which aims to allow idleness between calls to
  // advanceTime().
  @Test
  fun testDispatcher_hasPendingCompletableTasks_advanceBeforeTask_returnsTrue() {
    // Use a longer task time to avoid inadvertently running the task.
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(shortTaskDelayMillis - 1)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Any scheduled task is considered executing.
    assertThat(hasCompletableTasks).isTrue()
  }

  // For similar reasons, testing the 'true' case requires some deviation here. Tasks are considered
  // pending & completable immediately after being scheduled in a real threaded environment, unlike
  // the Robolectric variant of TestCoroutineDispatcher.
  @Test
  fun testDispatcher_hasPendingCompletableTasks_futureTask_returnsTrue() {
    // Use a longer delay to provide time for the pending tasks check.
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // The immediate task is completable now.
    assertThat(hasCompletableTasks).isTrue()
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

    @Provides
    @IsOnRobolectric
    fun provideIsOnRobolectric(): Boolean = false
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

    fun inject(test: TestCoroutineDispatcherEspressoImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTestCoroutineDispatcherEspressoImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: TestCoroutineDispatcherEspressoImplTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}

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
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.verifyZeroInteractions
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.testing.time.FakeSystemClock
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TestCoroutineDispatcherRobolectricImpl]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TestCoroutineDispatcherRobolectricImplTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TestCoroutineDispatcherRobolectricImplTest : TestCoroutineDispatcherTestBase(
  shortTaskDelayMillis = 100L,
  longTaskDelayMillis = 100L,
  // Exact matching isn't essential for correctness, and a nonzero value avoids an invalid range
  // issue in the base tests.
  longTaskDelayDeltaCheckMillis = 1L
) {
  @Inject
  lateinit var fakeSystemClock: FakeSystemClock

  @Before
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  override fun setUp() {
    setUpTestApplicationComponent()
    verifyDispatcherImplementation<TestCoroutineDispatcherRobolectricImpl>()
  }

  override fun getCurrentTimeMillis(): Long = fakeSystemClock.getTimeMillis()

  override fun advanceTimeBy(timeMillis: Long) {
    fakeSystemClock.advanceTime(timeMillis)
  }

  override fun stabilizeAfterDispatcherFlush() {
    // Nothing to do. The Robolectric implementation of the dispatcher guarantees completion after
    // runCurrent() is called.
  }

  override fun ensureFutureTasksAreScheduled() {
    backgroundTestDispatcher.runCurrent()
  }

  /*
   * Tests that cannot reliably be tested with real threads (i.e. using the Espresso impl). This is
   * often due to trying to coordinate immediate/future tasks, or trying to verify state after a
   * future task can start running but before it actually executes.
   */

  @Test
  fun testDispatcher_scheduleImmediateTask_doesNotRun() {
    scheduleImmediateTask(mockRunnable1)

    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_prepareFutureTask_doesNotRun() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    verify(mockRunnable1, never()).run()
  }

  // Highly time-sensitive test that's difficult to verify in the Espresso world.
  @Test
  fun testDispatcher_scheduleFutureTask_advanceTimeToTaskTime_doesNotRun() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(shortTaskDelayMillis)

    // Advancing time without running the dispatcher will not result in the task being called.
    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateTask_noCallbacks() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    reset(mockTaskIdleListener)

    scheduleImmediateTask(mockRunnable1)

    verifyZeroInteractions(mockTaskIdleListener)
  }

  // This test is excluded from the Espresso pseudo-environment because it relies specifically on
  // the task which schedules the delay to not run--this is hard to manage in a real threaded
  // environment.
  @Test
  fun testDispatcher_taskIdleListener_scheduleFutureTask_noCallbacks() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    reset(mockTaskIdleListener)

    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)

    verifyZeroInteractions(mockTaskIdleListener)
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateFutureTask_runBothTogether_calledOnce() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)
    reset(mockTaskIdleListener)

    advanceTimeBy(shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // The callbacks should be called once for both tasks. Note that order can't be reliably checked
    // here due to multiple tasks being executed in the same time quantum.
    verify(mockTaskIdleListener, atLeastOnce()).onDispatcherRunning()
    verify(mockTaskIdleListener, atLeastOnce()).onDispatcherIdle()
    verifyNoMoreInteractions(mockTaskIdleListener)
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateFutureTask_runCurrent_runningIdleCalled() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)
    reset(mockTaskIdleListener)

    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // The callbacks should be called for the first task. Note that order can't be reliably checked
    // here due to multiple tasks being executed in the same time quantum
    verify(mockTaskIdleListener, atLeastOnce()).onDispatcherRunning()
    verify(mockTaskIdleListener, atLeastOnce()).onDispatcherIdle()
    verifyNoMoreInteractions(mockTaskIdleListener)
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateFutureTask_runBothSeparately_calledTwice() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)
    reset(mockTaskIdleListener)

    // Run the tasks in 2 phases.
    backgroundTestDispatcher.runCurrent()
    advanceTimeBy(shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // The callbacks should be called multiple times. The number of times is implementation
    // dependent.
    verify(mockTaskIdleListener, atLeast(2)).onDispatcherIdle()
    verify(mockTaskIdleListener, atLeast(2)).onDispatcherRunning()
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateTask_runCurrent_isRunningThenIdle() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    reset(mockTaskIdleListener)

    scheduleImmediateTask(mockRunnable1)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // The listener should have entered a running state, then become idle immediately after the task
    // was completed.
    val inOrder = inOrder(mockTaskIdleListener)
    inOrder.verify(mockTaskIdleListener).onDispatcherRunning()
    inOrder.verify(mockTaskIdleListener).onDispatcherIdle()
    inOrder.verifyNoMoreInteractions()
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateTask_runCurrentTwice_callbacksCalledOnce() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    reset(mockTaskIdleListener)

    scheduleImmediateTask(mockRunnable1)
    // Intentionally call runCurrent twice.
    backgroundTestDispatcher.runCurrent()
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // The callbacks shouldn't be called again.
    val inOrder = inOrder(mockTaskIdleListener)
    inOrder.verify(mockTaskIdleListener).onDispatcherRunning()
    inOrder.verify(mockTaskIdleListener).onDispatcherIdle()
    inOrder.verifyNoMoreInteractions()
  }

  @Test
  fun testDispatcher_hasPendingTasks_oneImmediateTask_returnsTrue() {
    scheduleImmediateTask(mockRunnable1)

    val hasPendingTasks = backgroundTestDispatcher.hasPendingTasks()

    assertThat(hasPendingTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_oneImmediateTask_returnsTrue() {
    scheduleImmediateTask(mockRunnable1)

    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // An immediate task is completable now.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_oneFutureTask_returnsFalse() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Future tasks aren't yet completable.
    assertThat(hasCompletableTasks).isFalse()
  }

  // Note that this test's behavior differs from the Espresso variant. See the Espresso impl test
  // suite for more details.
  @Test
  fun testDispatcher_hasPendingCompletableTasks_advanceBeforeTask_returnsFalse() {
    // Use a longer task time to avoid inadvertently running the task.
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(shortTaskDelayMillis - 1)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // The clock hasn't yet advanced far enough for the task to be completable.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_immediateAndFutureTask_returnsTrue() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // The immediate task is completable now.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasCompletableTasks_immediateFutureTask_runCurrentThenAdvance_returnsTrue() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()
    advanceTimeBy(shortTaskDelayMillis)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Advancing time after completing the immediate task should yield completable tasks.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  @Suppress("UnnecessaryVariable") // Extra variables are for readability.
  fun testDispatcher_hasCompletableTasks_twoFutureTasks_runOne_thenAdvance_returnsTrue() {
    val taskDelayMs1 = shortTaskDelayMillis
    val taskDelayMs2 = taskDelayMs1 + shortTaskDelayMillis
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    advanceTimeBy(shortTaskDelayMillis)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Finishing the first task and stepping to the second results in the second now being
    // completable.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_advanceToTask_returnsTrue() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(shortTaskDelayMillis)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // The scheduled task is now completable.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_advancePastTask_returnsTrue() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(shortTaskDelayMillis + 1)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // The scheduled task has been completable.
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
    fun provideIsOnRobolectric(): Boolean = true
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

    fun inject(test: TestCoroutineDispatcherRobolectricImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTestCoroutineDispatcherRobolectricImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: TestCoroutineDispatcherRobolectricImplTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}

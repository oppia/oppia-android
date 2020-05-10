package org.oppia.testing

import android.os.SystemClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InternalCoroutinesApi
class TestCoroutineDispatchers @Inject constructor(
  @BackgroundTestDispatcher private val backgroundTestDispatcher: TestCoroutineDispatcher,
  @BlockingTestDispatcher private val blockingTestDispatcher: TestCoroutineDispatcher,
  private val fakeSystemClock: FakeSystemClock
) {
  private val shadowUiLooper = ShadowLooper.shadowMainLooper()

  @ExperimentalCoroutinesApi
  fun runCurrent() {
    do {
      flushNextTasks()
    } while (hasPendingCompletableTasks())
  }

  @ExperimentalCoroutinesApi
  fun advanceTimeBy(delayTimeMillis: Long) {
    fakeSystemClock.advanceTime(delayTimeMillis)
    runCurrent()
  }

  @ExperimentalCoroutinesApi
  fun advanceUntilIdle() {
    do {
      flushAllTasks()
    } while (hasPendingTasks())
  }

  @ExperimentalCoroutinesApi
  private fun flushNextTasks() {
    if (backgroundTestDispatcher.hasPendingCompletableTasks()) {
      backgroundTestDispatcher.runCurrent()
    }
    if (blockingTestDispatcher.hasPendingCompletableTasks()) {
      blockingTestDispatcher.runCurrent()
    }
    if (!shadowUiLooper.isIdle) {
      shadowUiLooper.idle()
    }
  }

  @ExperimentalCoroutinesApi
  private fun flushAllTasks() {
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    if (backgroundTestDispatcher.hasPendingCompletableTasks()) {
      backgroundTestDispatcher.advanceUntilIdle()
    }
    if (blockingTestDispatcher.hasPendingCompletableTasks()) {
      blockingTestDispatcher.advanceUntilIdle()
    }
    shadowUiLooper.idleFor(currentTimeMillis, TimeUnit.MILLISECONDS)
    SystemClock.setCurrentTimeMillis(currentTimeMillis)
  }

  private fun hasPendingTasks(): Boolean {
    // TODO: make idle check correct for all scheduled tasks
    return backgroundTestDispatcher.hasPendingTasks()
        || blockingTestDispatcher.hasPendingTasks()
        || !shadowUiLooper.isIdle
  }

  private fun hasPendingCompletableTasks(): Boolean {
    return backgroundTestDispatcher.hasPendingCompletableTasks()
        || blockingTestDispatcher.hasPendingCompletableTasks()
        || !shadowUiLooper.isIdle
  }
}

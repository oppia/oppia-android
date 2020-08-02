package org.oppia.testing

import android.os.SystemClock
import org.robolectric.Robolectric
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

// TODO(#89): Actually finish this implementation so that it properly works across Robolectric and
//  Espresso, and add tests for it.
/**
 * A Robolectric-specific fake for the system clock that can be used to manipulate time in a
 * consistent way.
 */
@Singleton
class FakeSystemClock @Inject constructor() {
  private val currentTimeMillis: AtomicLong

  init {
    val initialMillis = Robolectric.getForegroundThreadScheduler().currentTime
    SystemClock.setCurrentTimeMillis(initialMillis)
    currentTimeMillis = AtomicLong(initialMillis)
  }

  /** Returns the current time of the fake clock, in milliseconds. */
  fun getTimeMillis(): Long = currentTimeMillis.get()

  /**
   * Advances the clock time by the specific number of milliseconds, and returns the new value. It's
   * recommended to *never* use this method directly as it may result in UI-scheduled tasks
   * executing before background tasks, and may cause background tasks to execute at the wrong time.
   * If a test needs time to be advanced, it should use [TestCoroutineDispatchers.advanceTimeBy].
   */
  fun advanceTime(millis: Long): Long {
    val newTime = currentTimeMillis.addAndGet(millis)
    Robolectric.getForegroundThreadScheduler().advanceTo(newTime)
    SystemClock.setCurrentTimeMillis(newTime)
    return newTime
  }
}

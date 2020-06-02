package org.oppia.testing

import android.os.SystemClock
import org.robolectric.Robolectric
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

// TODO(#89): Actually finish this implementation so that it properly works across Robolectric and
//  Espresso, and add tests for it.
/**
 * A Robolectric-specific fake for the system clock that can be used to manipulate time in a
 * consistent way.
 */
class FakeSystemClock @Inject constructor() {
  private val currentTimeMillis: AtomicLong

  init {
    val initialMillis = Robolectric.getForegroundThreadScheduler().currentTime
    SystemClock.setCurrentTimeMillis(initialMillis)
    currentTimeMillis = AtomicLong(initialMillis)
  }

  fun getTimeMillis(): Long = currentTimeMillis.get()

  fun advanceTime(millis: Long): Long {
    val newTime = currentTimeMillis.addAndGet(millis)
    Robolectric.getForegroundThreadScheduler().advanceTo(newTime)
    SystemClock.setCurrentTimeMillis(newTime)
    return newTime
  }
}

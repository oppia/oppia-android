package org.oppia.testing

import android.os.SystemClock
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class FakeSystemClock @Inject constructor() {
  private val currentTimeMillis = AtomicLong(0L)

  init {
    SystemClock.setCurrentTimeMillis(0)
  }

  fun getTimeMillis(): Long = currentTimeMillis.get()

  fun advanceTime(millis: Long): Long {
    val newTime = currentTimeMillis.addAndGet(millis)
    SystemClock.setCurrentTimeMillis(newTime)
    return newTime
  }
}

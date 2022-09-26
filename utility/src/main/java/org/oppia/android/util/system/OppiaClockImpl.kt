package org.oppia.android.util.system

import android.os.SystemClock
import javax.inject.Inject

/** Implementation of [OppiaClock] that uses real time dependencies. */
class OppiaClockImpl @Inject constructor() : OppiaClock {
  override fun getCurrentTimeMs(): Long = System.currentTimeMillis()

  override fun getElapsedRealTime(): Long = SystemClock.elapsedRealtime()
}

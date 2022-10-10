package org.oppia.android.util.system

import javax.inject.Inject

/** Implementation of [OppiaClock] that uses real time dependencies. */
class OppiaClockImpl @Inject constructor() : OppiaClock {
  override fun getCurrentTimeMs(): Long = System.currentTimeMillis()
}

package org.oppia.android.testing.robolectric

import android.net.TrafficStats
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

private const val DEFAULT_BYTE_VALUE = 0L
private var uidTxBytes = DEFAULT_BYTE_VALUE
private var uidRxBytes = DEFAULT_BYTE_VALUE

/**
 * Shadows the Traffic Stats to extend its testing capabilities.
 *
 * There is an existing robolectric shadow of Traffic Stats but that doesn't provide us enough
 * control over network bytes sent/received for a specific UID and hence network usage can't be
 * tested using that.
 */
@Implements(TrafficStats::class)
class OppiaShadowTrafficStats {

  /** Sets [uidTxBytes] as equal to [bytes]. */
  fun setUidTxBytes(bytes: Long) {
    uidTxBytes = bytes
  }

  /** Sets [uidRxBytes] as equal to [bytes]. */
  fun setUidRxBytes(bytes: Long) {
    uidRxBytes = bytes
  }

  companion object {
    /**
     * Robolectric shadow override of [TrafficStats.getUidRxBytes]. Note that the value of
     * [uid] isn't taken into account in this implementation unlike the actual one.
     */
    @Implementation
    @JvmStatic
    fun getUidRxBytes(uid: Int): Long = uidRxBytes

    /**
     * Robolectric shadow override of [TrafficStats.getUidTxBytes]. Note that the value of
     * [uid] isn't taken into account in this implementation unlike the actual one.
     */
    @Implementation
    @JvmStatic
    fun getUidTxBytes(uid: Int): Long = uidTxBytes

    /**
     * Resets [uidTxBytes] and [uidRxBytes] values. This should always be called in a tear-down
     * method to avoid leaking state between tests.
     */
    fun reset() {
      uidRxBytes = DEFAULT_BYTE_VALUE
      uidTxBytes = DEFAULT_BYTE_VALUE
    }
  }
}

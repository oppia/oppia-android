package org.oppia.android.data.backends.gae

import javax.inject.Inject

/** Real implementation of [NetworkDelayHandler] that calls [Thread.sleep]. */
class RealNetworkDelayHandler @Inject constructor() : NetworkDelayHandler {
  override fun delay(millis: Long) {
    Thread.sleep(millis)
  }
}

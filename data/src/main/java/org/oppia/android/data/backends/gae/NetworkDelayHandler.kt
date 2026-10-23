package org.oppia.android.data.backends.gae

/** Handler to delay network interceptors in a testable way. */
interface NetworkDelayHandler {
  /** Delays the current thread by the specified milliseconds. */
  fun delay(millis: Long)
}

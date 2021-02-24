package org.oppia.android.util.logging

import android.util.Log

/** Corresponds to different severities of logs. */
enum class LogLevel constructor(internal val logLevel: Int) {
  VERBOSE(Log.VERBOSE),
  DEBUG(Log.DEBUG),
  INFO(Log.INFO),
  WARNING(Log.WARN),
  ERROR(Log.ERROR)
}

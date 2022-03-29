package org.oppia.android.util.logging

/** Provider for [ConsoleLoggerInjector]s. To be implemented by the application class. */
interface ConsoleLoggerInjectorProvider {
  /** Returns the [ConsoleLoggerInjector] corresponding to the current application context. */
  fun getConsoleLoggerInjector(): ConsoleLoggerInjector
}

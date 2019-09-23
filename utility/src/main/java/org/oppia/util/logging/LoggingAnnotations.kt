package org.oppia.util.logging

import javax.inject.Qualifier

/** Corresponds to a singleton boolean of whether console (logcat) logging is enabled. */
@Qualifier annotation class EnableConsoleLog

/** Corresponds to a singleton boolean of whether logs are saved to a file. */
@Qualifier annotation class EnableFileLog

/** Corresponds to a singleton [LogLevel] determining the minimum severity of logs that should be kept. */
@Qualifier annotation class GlobalLogLevel

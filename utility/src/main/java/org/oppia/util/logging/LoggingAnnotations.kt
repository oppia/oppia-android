package org.oppia.util.logging

import javax.inject.Qualifier

/** Corresponds to a singleton boolean of whether console (logcat) logging is enabled. */
@Qualifier internal annotation class EnableConsoleLog

/** Corresponds to a singleton boolean of whether logs are saved to a file. */
@Qualifier internal annotation class EnableFileLog

/** Corresponds to a singleton [LogLevel] determining the minimum severity of logs that should be kept. */
@Qualifier internal annotation class GlobalLogLevel

package org.oppia.android.util.logging

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

// TODO(#59): Introduce flavor-specific modules that configure logging settings based on what's reasonable (e.g. prod
// builds ought to not include verbose logging).
/** Provides logging-based dependencies. */
@Module
class LoggerModule {
  @Provides
  @EnableConsoleLog
  @Singleton
  fun provideEnableConsoleLog(): Boolean {
    return true
  }

  @Provides
  @EnableFileLog
  @Singleton
  fun provideEnableFileLog(): Boolean {
    return true
  }

  @Provides
  @GlobalLogLevel
  @Singleton
  fun provideGlobalLogLevel(): LogLevel {
    // By default, log everything.
    return LogLevel.VERBOSE
  }
}

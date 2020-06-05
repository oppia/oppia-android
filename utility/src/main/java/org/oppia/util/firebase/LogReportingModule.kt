package org.oppia.util.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Provides crash logging dependencies.
 */
@Module
class LogReportingModule {
  @Singleton
  @Provides
  fun provideCrashLogger(): CrashLogger {
    return CrashLoggerImplementation(FirebaseCrashlytics.getInstance())
  }

  @Singleton
  @Provides
  fun provideEventLogger(): EventLogger {
    return EventLoggerImplementation()
  }
}

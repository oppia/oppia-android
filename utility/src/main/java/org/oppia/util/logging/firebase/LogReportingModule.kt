package org.oppia.util.logging.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.ExceptionLogger
import javax.inject.Singleton

/** Provides Firebase-specific logging implementations. */
@Module
class LogReportingModule {
  @Singleton
  @Provides
  fun provideCrashLogger(): ExceptionLogger {
    return FirebaseExceptionLogger(FirebaseCrashlytics.getInstance())
  }

  @Singleton
  @Provides
  fun provideEventLogger(): EventLogger {
    return FirebaseEventLogger()
  }
}

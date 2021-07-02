package org.oppia.android.util.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import org.oppia.android.util.logging.firebase.FirebaseExceptionLogger
import javax.inject.Singleton

/** Provides debug log reporting dependencies. */
@Module
class DebugLogReportingModule {
  @Singleton
  @Provides
  fun provideExceptionLogger(): ExceptionLogger {
    return FirebaseExceptionLogger(FirebaseCrashlytics.getInstance())
  }

  @Singleton
  @Provides
  fun provideDebugEventLogger(debugEventLogger: DebugEventLogger): EventLogger = debugEventLogger
}

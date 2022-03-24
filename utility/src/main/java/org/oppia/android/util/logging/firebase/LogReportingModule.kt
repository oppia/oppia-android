package org.oppia.android.util.logging.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.oppia.android.util.logging.EventLogger
import org.oppia.android.util.logging.ExceptionLogger

/** Provides Firebase-specific logging implementations. */
@Module
class LogReportingModule {
  @Provides
  @Singleton
  fun provideCrashLogger(): ExceptionLogger =
    FirebaseExceptionLogger(FirebaseCrashlytics.getInstance())

  @Provides
  @Singleton
  fun provideEventLogger(factory: FirebaseEventLogger.Factory): EventLogger = factory.create()
}

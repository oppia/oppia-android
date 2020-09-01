package org.oppia.util.logging.firebase

import android.app.Application
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import org.oppia.util.logging.EventBundleCreator
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
  fun provideEventLogger(context: Context): EventLogger {
    return FirebaseEventLogger(
      FirebaseAnalytics.getInstance(Application()),
      EventBundleCreator(),
      context
    )
  }
}

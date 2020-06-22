package org.oppia.util.logging.firebase

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import org.oppia.util.logging.EnableDataCollection
import org.oppia.util.logging.EventBundleCreator
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.ExceptionLogger
import javax.inject.Singleton

/** Provides Firebase-specific logging implementations. */
@Module
class LogReportingModule {

  @Provides
  @EnableDataCollection
  @Singleton
  fun enableDataCollection(): Boolean {
    return false
  }

  @Singleton
  @Provides
  fun provideCrashLogger(): ExceptionLogger {
    return if (enableDataCollection()) {
      FirebaseExceptionLogger(FirebaseCrashlytics.getInstance())
    } else {
      StubbedEventLogger()
    }
  }

  @Singleton
  @Provides
  fun provideEventLogger(): EventLogger {
    return if (enableDataCollection()) {
      FirebaseEventLogger(
        FirebaseAnalytics.getInstance(Application()),
        EventBundleCreator()
      )
    } else {
      StubbedEventLogger()
    }
  }
}

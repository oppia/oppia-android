package org.oppia.android.util.logging.firebase

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import org.oppia.android.util.logging.EventBundleCreator
import org.oppia.android.util.logging.EventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.networking.NetworkConnectionUtil
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
  fun provideEventLogger(
    networkConnectionUtil: NetworkConnectionUtil
  ): EventLogger {
    return FirebaseEventLogger(
      FirebaseAnalytics.getInstance(Application()),
      EventBundleCreator(),
      networkConnectionUtil
    )
  }
}

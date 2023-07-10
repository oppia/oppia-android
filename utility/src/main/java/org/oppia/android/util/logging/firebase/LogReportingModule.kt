package org.oppia.android.util.logging.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.oppia.android.util.firestore.DataLogger
import org.oppia.android.util.firestore.FirestoreDataLogger
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger

/** Provides Firebase-specific logging implementations. */
@Module
class LogReportingModule {
  @Provides
  @Singleton
  fun provideCrashLogger(): ExceptionLogger =
    FirebaseExceptionLogger(FirebaseCrashlytics.getInstance())

  @Provides
  @Singleton
  fun provideFirebaseAnalyticsEventLogger(factory: FirebaseAnalyticsEventLogger.Factory):
    AnalyticsEventLogger = factory.create()

  @Provides
  @Singleton
  fun providePerformanceMetricsEventLogger(
    factory: FirebaseAnalyticsEventLogger.Factory
  ): PerformanceMetricsEventLogger =
    factory.createPerformanceMetricEventLogger()

  @Provides
  @Singleton
  fun provideFirestoreDataLogger(factory: FirestoreDataLogger.Factory): DataLogger =
    factory.createFirestoreDataLogger()
}

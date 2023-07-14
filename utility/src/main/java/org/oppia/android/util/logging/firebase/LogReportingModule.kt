package org.oppia.android.util.logging.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import org.oppia.android.util.firestore.DataUploader
import org.oppia.android.util.firestore.SurveyFirestoreDataUploader
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import javax.inject.Singleton

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
  fun provideFirestoreDataLogger(factory: SurveyFirestoreDataUploader.Factory): DataUploader =
    factory.createFirestoreDataLogger()
}

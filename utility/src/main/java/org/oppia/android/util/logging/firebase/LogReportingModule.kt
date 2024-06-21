package org.oppia.android.util.logging.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.BindsOptionalOf
import dagger.Provides
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import javax.inject.Singleton

/** Provides Firebase-specific logging implementations. */
@Module
interface LogReportingModule {
  companion object {
    @Provides
    @Singleton
    fun provideCrashLogger(): ExceptionLogger =
      FirebaseExceptionLogger(FirebaseCrashlytics.getInstance())

    @Provides
    @Singleton
    fun provideFirebaseAnalyticsEventLogger(
      factory: FirebaseAnalyticsEventLogger.FactoryImpl
    ): AnalyticsEventLogger = factory.create()

    @Provides
    @Singleton
    fun providePerformanceMetricsEventLogger(
      factory: FirebaseAnalyticsEventLogger.FactoryImpl
    ): PerformanceMetricsEventLogger = factory.createPerformanceMetricEventLogger()

    @Provides
    @Singleton
    fun provideFirestoreLogger(
      factory: FirestoreEventLoggerProdImpl
    ): FirestoreEventLogger = factory

    @Provides
    @Singleton
    fun provideFirebaseFirestoreInstanceWrapper(
      impl: FirestoreInstanceWrapperImpl
    ): FirestoreInstanceWrapper = impl
  }

  @BindsOptionalOf fun bindOptionalDebugAnalyticsEventLogger(): DebugAnalyticsEventLogger
}

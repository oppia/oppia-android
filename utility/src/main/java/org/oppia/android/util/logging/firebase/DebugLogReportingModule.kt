package org.oppia.android.util.logging.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Provides
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import javax.inject.Qualifier
import javax.inject.Singleton

/** Provides debug log reporting dependencies. */
@Module
interface DebugLogReportingModule {
  companion object {
    @Provides
    @Singleton
    fun provideExceptionLogger(): ExceptionLogger =
      FirebaseExceptionLogger(FirebaseCrashlytics.getInstance())

    @Provides
    @Singleton
    @ImplementationEventLogger
    fun provideImplementationDebugEventLogger(
      debugLoggerFactory: DebugAnalyticsEventLogger.Factory,
      firebaseLoggerFactory: FirebaseAnalyticsEventLogger.FactoryImpl
    ): DebugAnalyticsEventLogger = debugLoggerFactory.create(firebaseLoggerFactory)

    @Provides
    @Singleton
    fun providePerformanceMetricsEventLogger(
      factory: FirebaseAnalyticsEventLogger.FactoryImpl
    ): PerformanceMetricsEventLogger = factory.createPerformanceMetricEventLogger()

    @Provides
    @Singleton
    fun provideDebugFirestoreLogger(
      debugLogger: DebugFirestoreEventLoggerImpl
    ): FirestoreEventLogger = debugLogger

    @Provides
    @Singleton
    fun provideFirebaseFirestoreInstanceWrapper(
      impl: FirestoreInstanceWrapperImpl
    ): FirestoreInstanceWrapper = impl
  }

  @Binds
  fun provideDebugAnalyticsEventLogger(
    @ImplementationEventLogger impl: DebugAnalyticsEventLogger
  ): DebugAnalyticsEventLogger

  @Binds
  fun provideAnalyticsEventLogger(
    @ImplementationEventLogger impl: DebugAnalyticsEventLogger
  ): AnalyticsEventLogger

  @BindsOptionalOf fun bindOptionalDebugAnalyticsEventLogger(): DebugAnalyticsEventLogger

  /**
   * Qualifier for this module's internal [AnalyticsEventLogger] implementation. This shouldn't be
   * used outside of the module.
   */
  @Qualifier annotation class ImplementationEventLogger
}

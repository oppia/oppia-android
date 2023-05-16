package org.oppia.android.testing

import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.firebase.DebugAnalyticsEventLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import javax.inject.Singleton

/** Provides fake log reporting dependencies. */
@Module
interface TestLogReportingModule {
  companion object {
    @Provides
    @Singleton
    fun provideFakeEventLogger(
      fakeLoggerFactory: FakeAnalyticsEventLogger.FactoryImpl
    ): FakeAnalyticsEventLogger = fakeLoggerFactory.create()
  }

  @Binds fun bindFakeEventLogger(impl: FakeAnalyticsEventLogger): AnalyticsEventLogger
  @Binds fun bindFakeExceptionLogger(impl: FakeExceptionLogger): ExceptionLogger

  @Binds
  fun bindFakePerformanceMetricsEventLogger(
    impl: FakePerformanceMetricsEventLogger
  ): PerformanceMetricsEventLogger

  @Binds
  fun bindFakePerformanceMetricsAssessor(
    impl: FakePerformanceMetricAssessor
  ): PerformanceMetricsAssessor

  @BindsOptionalOf fun bindOptionalDebugAnalyticsEventLogger(): DebugAnalyticsEventLogger
}

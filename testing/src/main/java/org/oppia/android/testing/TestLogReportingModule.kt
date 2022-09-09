package org.oppia.android.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger

/** Provides fake log reporting dependencies. */
@Module
interface TestLogReportingModule {

  @Binds
  fun bindFakeExceptionLogger(fakeExceptionLogger: FakeExceptionLogger): ExceptionLogger

  @Binds
  fun bindFakeAnalyticsEventLogger(fakeAnalyticsEventLogger: FakeAnalyticsEventLogger): AnalyticsEventLogger

  @Binds
  fun bindFakePerformanceMetricsEventLogger(
    fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
  ): PerformanceMetricsEventLogger

  @Binds
  fun bindFakePerformanceMetricsAssessor(
    fakePerformanceMetricAssessor: FakePerformanceMetricAssessor
  ): PerformanceMetricsAssessor
}

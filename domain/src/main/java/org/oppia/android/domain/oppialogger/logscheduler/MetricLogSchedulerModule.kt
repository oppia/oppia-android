package org.oppia.android.domain.oppialogger.logscheduler

import dagger.Module
import dagger.Provides
import org.oppia.android.util.logging.MetricLogScheduler

/** Provides Log Generator related dependencies. */
@Module
class MetricLogSchedulerModule {
  @Provides
  fun provideMetricLogScheduler(
    performanceMetricLogScheduler: PerformanceMetricsLogScheduler
  ): MetricLogScheduler = performanceMetricLogScheduler
}

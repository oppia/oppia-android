package org.oppia.android.domain.oppialogger.logscheduler

import dagger.Binds
import dagger.Module
import org.oppia.android.util.logging.MetricLogScheduler

/** Provides metric log scheduler related dependencies. */
@Module
abstract class MetricLogSchedulerModule {
  @Binds
  abstract fun provideMetricLogScheduler(
    performanceMetricLogScheduler: PerformanceMetricsLogScheduler
  ): MetricLogScheduler
}

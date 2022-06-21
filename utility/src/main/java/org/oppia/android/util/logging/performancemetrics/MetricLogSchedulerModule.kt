package org.oppia.android.util.logging.performancemetrics

import dagger.Binds
import dagger.Module
import org.oppia.android.util.logging.MetricLogScheduler

/** Provides Log Generator related dependencies. */
@Module
interface MetricLogSchedulerModule {
  @Binds
  fun bindMetricLogScheduler(
    performanceMetricLogScheduler: PerformanceMetricsLogScheduler
  ): MetricLogScheduler
}

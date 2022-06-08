package org.oppia.android.util.logging.performancemetrics

import dagger.Binds
import dagger.Module
import org.oppia.android.util.logging.LogGenerator

/** Provides Log Generator related dependencies. */
@Module
interface MetricLogGeneratorModule {
  @Binds
  fun bindMetricLogGenerator(metricLogGenerator: MetricLogGenerator): LogGenerator
}
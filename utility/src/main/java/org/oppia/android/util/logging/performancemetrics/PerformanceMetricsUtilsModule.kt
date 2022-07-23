package org.oppia.android.util.logging.performancemetrics

import dagger.Binds
import dagger.Module

@Module
interface PerformanceMetricsUtilsModule {
  @Binds
  fun bindPerformanceMetricUtils(
    prodPerformanceMetricsUtils: ProdPerformanceMetricsUtils
  ): PerformanceMetricsUtils
}

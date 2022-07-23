package org.oppia.android.util.logging.performancemetrics

import dagger.Binds
import dagger.Module

/** Provides production-specific performance metrics utilities related dependencies. */
@Module
interface PerformanceMetricsUtilsModule {
  @Binds
  fun bindPerformanceMetricUtils(
    prodPerformanceMetricsUtils: ProdPerformanceMetricsUtils
  ): PerformanceMetricsUtils
}

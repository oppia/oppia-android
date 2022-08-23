package org.oppia.android.util.logging.performancemetrics

import dagger.Module
import dagger.Provides

/** Provides production-specific performance metrics utilities related dependencies. */
@Module
class PerformanceMetricsAssessorModule {

  @Provides
  fun providePerformanceMetricsAssessor(
    performanceMetricsAssessorImpl: PerformanceMetricsAssessorImpl
  ): PerformanceMetricsAssessor = performanceMetricsAssessorImpl
}

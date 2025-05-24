package org.oppia.android.util.logging.performancemetrics.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.testing.FakePerformanceMetricAssessor
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor

@Module
interface PerformanceMetricsAssessorTestModule {
  @Binds
  fun bindFakePerformanceMetricsAssessor(
    fakePerformanceMetricAssessor: FakePerformanceMetricAssessor
  ): PerformanceMetricsAssessor
}

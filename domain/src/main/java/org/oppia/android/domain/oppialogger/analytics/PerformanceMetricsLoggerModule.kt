package org.oppia.android.domain.oppialogger.analytics

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** Binds [PerformanceMetricsLogger] as an [ApplicationStartupListener] */
@Module
class PerformanceMetricsLoggerModule {
  @Provides
  @IntoSet
  fun providePerformanceMetricsLogger(
    performanceMetricsLogger: PerformanceMetricsLogger
  ): ApplicationStartupListener = performanceMetricsLogger
}

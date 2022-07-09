package org.oppia.android.domain.oppialogger.analytics

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** Binds [PerformanceMetricsLogger] as an [ApplicationStartupListener] */
@Module
interface PerformanceMetricsLoggerModule {
  @Binds
  @IntoSet
  fun bindPerformanceMetricsLogger(
    performanceMetricsLogger: PerformanceMetricsLogger
  ): ApplicationStartupListener
}

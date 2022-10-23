package org.oppia.android.domain.oppialogger.analytics

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor
import org.oppia.android.util.threading.BackgroundDispatcher
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/** Provides dependencies that are needed for logging CPU usage. */
@Module
class CpuPerformanceSnapshotterModule {

  @Singleton
  @Provides
  fun providesCpuPerformanceSnapshotter(
    performanceMetricsLogger: PerformanceMetricsLogger,
    consoleLogger: ConsoleLogger,
    exceptionLogger: ExceptionLogger,
    @ForegroundCpuLoggingTimePeriodMillis foregroundCpuLoggingTimePeriodMillis: Long,
    @BackgroundCpuLoggingTimePeriodMillis backgroundCpuLoggingTimePeriodMillis: Long,
    @BackgroundDispatcher backgroundCoroutineDispatcher: CoroutineDispatcher,
    performanceMetricsAssessor: PerformanceMetricsAssessor
  ): CpuPerformanceSnapshotter = CpuPerformanceSnapshotter(
    backgroundCoroutineDispatcher,
    performanceMetricsLogger,
    PerformanceMetricsAssessor.AppIconification.APP_IN_BACKGROUND,
    consoleLogger,
    exceptionLogger,
    foregroundCpuLoggingTimePeriodMillis,
    backgroundCpuLoggingTimePeriodMillis,
    performanceMetricsAssessor
  )

  @Provides
  @ForegroundCpuLoggingTimePeriodMillis
  fun provideForegroundCpuLoggingTimePeriodMillis(): Long = TimeUnit.MINUTES.toMillis(5)

  @Provides
  @BackgroundCpuLoggingTimePeriodMillis
  fun provideBackgroundCpuLoggingTimePeriodMillis(): Long = TimeUnit.MINUTES.toMillis(60)
}

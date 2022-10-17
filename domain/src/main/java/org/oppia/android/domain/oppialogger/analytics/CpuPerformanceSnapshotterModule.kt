package org.oppia.android.domain.oppialogger.analytics

import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit

/** Provides dependencies that are needed for logging cpu usage. */
@Module
class CpuPerformanceSnapshotterModule {

  @Provides
  fun providesCpuPerformanceSnapshotter(
    factory: CpuPerformanceSnapshotter.Factory
  ): CpuPerformanceSnapshotter = factory.createSnapshotter()

  @Provides
  @ForegroundCpuLoggingTimePeriodMillis
  fun provideForegroundCpuLoggingTimePeriodMillis(): Long = TimeUnit.MINUTES.toMillis(5)

  @Provides
  @BackgroundCpuLoggingTimePeriodMillis
  fun provideBackgroundCpuLoggingTimePeriodMillis(): Long = TimeUnit.MINUTES.toMillis(60)
}

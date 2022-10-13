package org.oppia.android.domain.oppialogger.analytics

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

private const val SIXTY_MINUTES_IN_MILLIS = 60 * 60 * 1000L
private const val FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000L

@Qualifier annotation class ForegroundCpuLoggingTimePeriod

@Qualifier annotation class BackgroundCpuLoggingTimePeriod

@Module
class CpuPerformanceSnapshotterModule {

  @Provides
  fun providesCpuPerformanceSnapshotter(
    factory: CpuPerformanceSnapshotter.Factory
  ): CpuPerformanceSnapshotter = factory.createSnapshotter()

  @Provides
  @ForegroundCpuLoggingTimePeriod
  fun provideForegroundCpuLoggingTimePeriod(): Long = FIVE_MINUTES_IN_MILLIS

  @Provides
  @BackgroundCpuLoggingTimePeriod
  fun provideBackgroundCpuLoggingTimePeriod(): Long = SIXTY_MINUTES_IN_MILLIS
}

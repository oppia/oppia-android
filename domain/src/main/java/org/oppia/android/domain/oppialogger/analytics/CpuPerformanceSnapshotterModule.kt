package org.oppia.android.domain.oppialogger.analytics

import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

/**
 * Corresponds to an injectable application-level [Long] that corresponds to the number of
 * milliseconds in which the foregrounded app logs another cpu usage metric event.
 */
@Qualifier annotation class ForegroundCpuLoggingTimePeriod

/**
 * Corresponds to an injectable application-level [Long] that corresponds to the number of
 * milliseconds in which the backgrounded app logs another cpu usage metric event.
 */
@Qualifier annotation class BackgroundCpuLoggingTimePeriod

/** Provides dependencies that are needed for logging cpu usage. */
@Module
class CpuPerformanceSnapshotterModule {

  @Provides
  fun providesCpuPerformanceSnapshotter(
    factory: CpuPerformanceSnapshotter.Factory
  ): CpuPerformanceSnapshotter = factory.createSnapshotter()

  @Provides
  @ForegroundCpuLoggingTimePeriod
  fun provideForegroundCpuLoggingTimePeriod(): Long = TimeUnit.MINUTES.toMillis(5)

  @Provides
  @BackgroundCpuLoggingTimePeriod
  fun provideBackgroundCpuLoggingTimePeriod(): Long = TimeUnit.MINUTES.toMillis(60)
}

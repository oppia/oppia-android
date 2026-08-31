package org.oppia.android.domain.oppialogger.analytics

import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit

/** Provides dependencies that are needed for logging CPU usage. */
@Module
class CpuPerformanceSnapshotterModule {
  @Provides
  @ForegroundCpuLoggingTimePeriodMillis
  fun provideForegroundCpuLoggingTimePeriodMillis(): Long = TimeUnit.MINUTES.toMillis(5)

  @Provides
  @BackgroundCpuLoggingTimePeriodMillis
  fun provideBackgroundCpuLoggingTimePeriodMillis(): Long = TimeUnit.MINUTES.toMillis(60)

  @Provides
  @InitialIconificationCutOffTimePeriodMillis
  fun provideInitialIconificationCutOffTimePeriodMillis(): Long = TimeUnit.SECONDS.toMillis(60)
}

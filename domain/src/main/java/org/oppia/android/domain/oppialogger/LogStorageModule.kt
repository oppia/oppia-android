package org.oppia.android.domain.oppialogger

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier
annotation class EventLogStorageCacheSize

@Qualifier
annotation class ExceptionLogStorageCacheSize

@Qualifier
annotation class MetricLogStorageCacheSize

/** Provider to return any constants required during the storage of log reports. */
@Module
class LogStorageModule {

  /**
   * Provides the maximum number of event logs that can be cached on disk.
   *
   * At a configured cache size of 50k records & estimating 70 bytes per record, it's expected that
   * no more than 3.33MB will be required for cache disk space.
   */
  @Provides
  @EventLogStorageCacheSize
  fun provideEventLogStorageCacheSize(): Int = 50_000

  /**
   * Provides the maximum number of exception logs that can be cached on disk.
   *
   * At a configured cache size of 25 records & estimating 130 bytes per record, it's expected that
   * no more than 3.17KB will be required for cache disk space.
   */
  @Provides
  @ExceptionLogStorageCacheSize
  fun provideExceptionLogStorageCacheSize(): Int = 25

  /**
   * Provides the maximum number of performance metrics logs that can be cached on disk.
   *
   * At a configured cache size of 25k records & estimating 121 bytes per record, it's expected that
   * no more than 3.02MB will be required for cache disk space.
   */
  @Provides
  @MetricLogStorageCacheSize
  fun provideMetricLogStorageCacheSize(): Int = 25_000
}

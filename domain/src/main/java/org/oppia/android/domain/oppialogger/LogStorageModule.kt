package org.oppia.android.domain.oppialogger

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier
annotation class EventLogStorageCacheSize

@Qualifier
annotation class ExceptionLogStorageCacheSize

/** Provider to return any constants required during the storage of log reports. */
@Module
class LogStorageModule {

  /**
   * Provides the number of records that can be stored in EventLog's cache storage.
   * The current [EventLogStorageCacheSize] is set to be 5000 records.
   * Taking 70 bytes per record, it is expected to occupy around 350000 bytes of disk space.
   */
  @Provides
  @EventLogStorageCacheSize
  fun provideEventLogStorageCacheSize(): Int = 5000

  /**
   * Provides the number of records that can be stored in ExceptionLog's cache storage.
   * The current [ExceptionLogStorageCacheSize] is set to be 25 records.
   * Taking 130 bytes per record, it is expected to occupy around 3250 bytes of disk space.
   */
  @Provides
  @ExceptionLogStorageCacheSize
  fun provideExceptionLogStorageCacheSize(): Int = 25
}

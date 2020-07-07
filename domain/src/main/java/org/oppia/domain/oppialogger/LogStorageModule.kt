package org.oppia.domain.oppialogger

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

  /** Provides the number of records that can be stored in EventLog's cache storage.*/
  @Provides
  @EventLogStorageCacheSize
  fun provideEventLogStorageCacheSize(): Int = 25

  /** Provides the number of records that can be stored in ExceptionLog's cache storage.*/
  @Provides
  @ExceptionLogStorageCacheSize
  fun provideExceptionLogStorageCacheSize(): Int = 25
}

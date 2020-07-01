package org.oppia.domain.analytics

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
  @Provides
  @EventLogStorageCacheSize
  fun provideEventLogStorageCacheSize(): Int = 10

  @Provides
  @ExceptionLogStorageCacheSize
  fun provideExceptionLogStorageCacheSize(): Int = 10
}

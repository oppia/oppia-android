package org.oppia.android.util.threading

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import javax.inject.Singleton

/**
 * Dagger [Module] that provides [CoroutineDispatcher]s that bind to [BackgroundDispatcher] and [BlockingDispatcher]
 * qualifiers.
 */
@Module
class DispatcherModule {
  @Provides
  @BackgroundExecutor
  @Singleton
  fun provideBackgroundExecutor(): ScheduledExecutorService =
    Executors.newScheduledThreadPool(/* corePoolSize= */ 4)

  @Provides
  @BlockingExecutor
  @Singleton
  fun provideBlockingExecutor(): ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor()

  @Provides
  @BackgroundDispatcher
  @Singleton
  fun provideBackgroundDispatcher(
    @BackgroundExecutor executorService: ScheduledExecutorService
  ): CoroutineDispatcher = executorService.asCoroutineDispatcher()

  @Provides
  @BlockingDispatcher
  @Singleton
  fun provideBlockingDispatcher(
    @BlockingExecutor executorService: ScheduledExecutorService
  ): CoroutineDispatcher = executorService.asCoroutineDispatcher()
}

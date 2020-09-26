package org.oppia.android.util.threading

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.inject.Singleton

/**
 * Dagger [Module] that provides [CoroutineDispatcher]s that bind to [BackgroundDispatcher] and [BlockingDispatcher]
 * qualifiers.
 */
@Module
class DispatcherModule {
  @Provides
  @BackgroundDispatcher
  @Singleton
  fun provideBackgroundDispatcher(): CoroutineDispatcher {
    return Executors.newFixedThreadPool(/* nThreads= */ 4).asCoroutineDispatcher()
  }

  @Provides
  @BlockingDispatcher
  @Singleton
  fun provideBlockingDispatcher(): CoroutineDispatcher {
    return Executors.newSingleThreadExecutor().asCoroutineDispatcher()
  }
}

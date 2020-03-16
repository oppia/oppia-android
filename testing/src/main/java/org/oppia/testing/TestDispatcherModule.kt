package org.oppia.testing

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import java.util.concurrent.Executors
import javax.inject.Singleton

/**
 * Dagger [Module] that provides [CoroutineDispatcher]s that bind to [BackgroundDispatcher] and [BlockingDispatcher]
 * qualifiers.
 */
@Module
class TestDispatcherModule {
  @Provides
  @InternalCoroutinesApi
  @BackgroundDispatcher
  fun provideBackgroundDispatcher(
    @BackgroundTestDispatcher testCoroutineDispatcher: TestCoroutineDispatcher
  ): CoroutineDispatcher {
    return testCoroutineDispatcher
  }

  @Provides
  @InternalCoroutinesApi
  @BlockingDispatcher
  fun provideBlockingDispatcher(
    @BlockingTestDispatcher testCoroutineDispatcher: TestCoroutineDispatcher
  ): CoroutineDispatcher {
    return testCoroutineDispatcher
  }

  @Provides
  @BackgroundTestDispatcher
  @InternalCoroutinesApi
  @Singleton
  fun provideBackgroundTestDispatcher(factory: TestCoroutineDispatcher.Factory): TestCoroutineDispatcher {
    return factory.createDispatcher(Executors.newFixedThreadPool(/* nThreads= */ 4).asCoroutineDispatcher())
  }

  @Provides
  @BlockingTestDispatcher
  @InternalCoroutinesApi
  @Singleton
  fun provideBlockingTestDispatcher(factory: TestCoroutineDispatcher.Factory): TestCoroutineDispatcher {
    return factory.createDispatcher(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
  }
}

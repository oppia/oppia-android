package org.oppia.android.testing.threading

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.util.threading.BackgroundDispatcher
import java.util.concurrent.Executors
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Dagger [Module] that provides [CoroutineDispatcher]s that binds test-specific
 * [TestCoroutineDispatcher] replacements for production-used coroutine dispatchers.
 */
@Module
class TestDispatcherModule {
  @Provides
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  @BackgroundDispatcher
  fun provideBackgroundDispatcher(
    @BackgroundTestDispatcher testCoroutineDispatcher: TestCoroutineDispatcher
  ): CoroutineDispatcher = testCoroutineDispatcher

  @Provides
  @BackgroundTestDispatcher
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  @Singleton
  fun provideBackgroundTestDispatcher(
    factory: TestCoroutineDispatcher.Factory
  ): TestCoroutineDispatcher {
    return factory.createDispatcher(
      Executors.newFixedThreadPool(/* nThreads= */ 4).asCoroutineDispatcher()
    )
  }

  @Provides
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun provideTestCoroutineDispatchers(
    @IsOnRobolectric isOnRobolectric: Boolean,
    robolectricImplProvider: Provider<TestCoroutineDispatchersRobolectricImpl>,
    espressoImplProvider: Provider<TestCoroutineDispatchersEspressoImpl>
  ): TestCoroutineDispatchers {
    return if (isOnRobolectric) robolectricImplProvider.get() else espressoImplProvider.get()
  }

  @Provides
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun provideTestCoroutineDispatcherFactory(
    @IsOnRobolectric isOnRobolectric: Boolean,
    robolectricFactoryProvider: Provider<TestCoroutineDispatcherRobolectricImpl.FactoryImpl>,
    espressoFactoryProvider: Provider<TestCoroutineDispatcherEspressoImpl.FactoryImpl>
  ): TestCoroutineDispatcher.Factory {
    return if (isOnRobolectric) robolectricFactoryProvider.get() else espressoFactoryProvider.get()
  }
}

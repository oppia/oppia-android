package org.oppia.testing

import android.os.Build
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import java.util.concurrent.Executors
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Dagger [Module] that provides [CoroutineDispatcher]s that bind to [BackgroundDispatcher] and
 * [BlockingDispatcher] qualifiers.
 */
@Module
class TestDispatcherModule {
  @Provides
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  @BackgroundDispatcher
  fun provideBackgroundDispatcher(
    @BackgroundTestDispatcher testCoroutineDispatcher: TestCoroutineDispatcher
  ): CoroutineDispatcher {
    return testCoroutineDispatcher
  }

  @Provides
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  @BlockingDispatcher
  fun provideBlockingDispatcher(
    @BlockingTestDispatcher testCoroutineDispatcher: TestCoroutineDispatcher
  ): CoroutineDispatcher {
    return testCoroutineDispatcher
  }

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
  @BlockingTestDispatcher
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  @Singleton
  fun provideBlockingTestDispatcher(
    factory: TestCoroutineDispatcher.Factory
  ): TestCoroutineDispatcher {
    return factory.createDispatcher(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
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

  @Provides
  @IsOnRobolectric
  @Singleton
  fun provideIsOnRobolectric(): Boolean {
    return Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
  }
}

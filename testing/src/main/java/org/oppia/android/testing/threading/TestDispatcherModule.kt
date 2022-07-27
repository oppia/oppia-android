package org.oppia.android.testing.threading

import com.google.common.base.Optional
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.BackgroundExecutor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Dagger [Module] that provides [CoroutineDispatcher]s that binds test-specific
 * [TestCoroutineDispatcher] replacements for production-used coroutine dispatchers.
 */
@Module
class TestDispatcherModule {
  @Provides
  @BackgroundExecutor
  fun provideBackgroundExecutor(
    @PrivateBackgroundExecutor executorService: CoordinatedScheduledExecutorService
  ): ScheduledExecutorService = executorService

  @Provides
  @GlideTestExecutor
  fun provideGlideTestExecutor(
    @PrivateGlideExecutor executorService: CoordinatedScheduledExecutorService
  ): ScheduledExecutorService = executorService

  @Provides
  @PrivateBackgroundBackingExecutor
  @Singleton
  fun provideBackgroundTestBackingExecutor(): ScheduledExecutorService =
    Executors.newScheduledThreadPool(/* corePoolSize= */ 4)

  @Provides
  @PrivateBackgroundExecutor
  @Singleton
  fun provideBackgroundTestExecutor(
    @Private factory: CoordinatedScheduledExecutorService.Factory,
    @PrivateBackgroundBackingExecutor backingExecutor: ScheduledExecutorService
  ): CoordinatedScheduledExecutorService = factory.create(backingExecutor)

  // Glide shares the executor service for general background tasks.
  @Provides
  @PrivateGlideExecutor
  fun providePrivateGlideTestExecutor(
    @PrivateBackgroundExecutor executorService: CoordinatedScheduledExecutorService
  ): CoordinatedScheduledExecutorService = executorService

  @Provides
  @IntoSet
  @CoordinatedTaskExecutor
  fun provideBackgroundTestExecutorForSet(
    @PrivateBackgroundExecutor executorService: CoordinatedScheduledExecutorService
  ): Optional<MonitoredTaskCoordinator> = Optional.of(executorService)

  @Provides
  @IntoSet
  @CoordinatedTaskExecutor
  fun provideGlideTestExecutorForSet(
    @PrivateGlideExecutor executorService: CoordinatedScheduledExecutorService
  ): Optional<MonitoredTaskCoordinator> = Optional.of(executorService)

  @Provides
  @IntoSet
  @CoordinatedTaskExecutor
  fun provideRobolectricUiTaskExecutorForSet(
    @IsOnRobolectric isOnRobolectric: Boolean,
    uiTaskExecutorProvider: Provider<MonitoredRobolectricUiTaskCoordinator>
  ): Optional<MonitoredTaskCoordinator> {
    return if (isOnRobolectric) Optional.of(uiTaskExecutorProvider.get()) else Optional.absent()
  }

  // TODO(#1720): Remove this once Hilt allows for configurable build graphs.
  @Provides
  fun provideAvailableMonitoredTaskCoordinators(
    @CoordinatedTaskExecutor coorders: Set<@JvmSuppressWildcards Optional<MonitoredTaskCoordinator>>
  ): Set<@JvmSuppressWildcards MonitoredTaskCoordinator> {
    return coorders.mapNotNull { it.orNull() }.toSet()
  }

  @Provides
  @BackgroundDispatcher
  @Singleton
  fun provideBackgroundDispatcher(
    @BackgroundExecutor executorService: ScheduledExecutorService
  ): CoroutineDispatcher = executorService.asCoroutineDispatcher()

  @Provides
  fun provideTestCoroutineDispatchers(
    @IsOnRobolectric isOnRobolectric: Boolean,
    robolectricImplProvider: Provider<TestCoroutineDispatchersRobolectricImpl>,
    espressoImplProvider: Provider<TestCoroutineDispatchersEspressoImpl>
  ): TestCoroutineDispatchers {
    return if (isOnRobolectric) robolectricImplProvider.get() else espressoImplProvider.get()
  }

  @Provides
  @Private
  fun providePlatformSpecificCoordinatedScheduledExecutorServiceFactory(
    @IsOnRobolectric isOnRobolectric: Boolean,
    blockingExecutorServiceFactoryProvider: Provider<BlockingScheduledExecutorService.FactoryImpl>,
    realTimeExecutorServiceFactoryProvider: Provider<RealTimeScheduledExecutorService.FactoryImpl>
  ): CoordinatedScheduledExecutorService.Factory {
    return if (isOnRobolectric) {
      blockingExecutorServiceFactoryProvider.get()
    } else realTimeExecutorServiceFactoryProvider.get()
  }

  @Qualifier private annotation class Private
  @Qualifier private annotation class PrivateBackgroundBackingExecutor
  @Qualifier private annotation class PrivateBackgroundExecutor
  @Qualifier private annotation class PrivateGlideExecutor
}

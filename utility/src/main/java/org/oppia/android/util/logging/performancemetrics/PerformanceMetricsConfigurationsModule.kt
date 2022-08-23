package org.oppia.android.util.logging.performancemetrics

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier
import org.oppia.android.app.model.OppiaMetricLog.StorageTier

private const val ONE_GIGABYTE = 1024L * 1024L * 1024L
private const val TWO_GIGABYTES = ONE_GIGABYTE * 2L
private const val THREE_GIGABYTES = ONE_GIGABYTE * 3L
private const val THIRTY_TWO_GIGABYTES = ONE_GIGABYTE * 36L
private const val SIXTY_FOUR_GIGABYTES = ONE_GIGABYTE * 64L

/** Corresponds to a long value that denotes the non-inclusive upper bound of [StorageTier.LOW_STORAGE]. */
@Qualifier
annotation class LowStorageTierUpperBound

/** Corresponds to a long value that denotes the inclusive upper bound of [StorageTier.MEDIUM_STORAGE]. */
@Qualifier
annotation class MediumStorageTierUpperBound

/** Corresponds to a long value that denotes the non-inclusive upper bound of [MemoryTier.LOW_MEMORY_TIER]. */
@Qualifier
annotation class LowMemoryTierUpperBound

/** Corresponds to a long value that denotes the inclusive upper bound of [MemoryTier.MEDIUM_MEMORY_TIER]. */
@Qualifier
annotation class MediumMemoryTierUpperBound

@Module
class PerformanceMetricsConfigurationsModule {
  @Provides
  @LowStorageTierUpperBound
  fun provideLowStorageTierUpperBound(): Long = THIRTY_TWO_GIGABYTES

  @Provides
  @MediumStorageTierUpperBound
  fun provideMediumStorageTierUpperBound(): Long = SIXTY_FOUR_GIGABYTES

  @Provides
  @LowMemoryTierUpperBound
  fun provideLowMemoryTierUpperBound(): Long = TWO_GIGABYTES

  @Provides
  @MediumMemoryTierUpperBound
  fun provideMediumMemoryTierUpperBound(): Long = THREE_GIGABYTES
}
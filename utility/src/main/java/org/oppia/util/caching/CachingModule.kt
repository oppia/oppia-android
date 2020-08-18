package org.oppia.util.caching

import dagger.Module
import dagger.Provides

private const val FRACTIONS_EXPLORATION_ID_0 = "umPkwp0L1M0-"
private const val FRACTIONS_EXPLORATION_ID_1 = "MjZzEVOG47_1"
private const val RATIOS_EXPLORATION_ID_0 = "2mzzFVDLuAj8"
private const val RATIOS_EXPLORATION_ID_1 = "5NWuolNcwH6e"
private const val RATIOS_EXPLORATION_ID_2 = "k2bQ7z5XHNbK"
private const val RATIOS_EXPLORATION_ID_3 = "tIoSb3HZFN6e"

/** Provides dependencies corresponding to the app's caching policies. */
@Module
class CachingModule {
  @Provides
  @CacheAssetsLocally
  fun provideCacheAssetsLocally(): Boolean = false

  @Provides
  @TopicListToCache
  fun provideTopicListToCache() = listOf(
    FRACTIONS_EXPLORATION_ID_0, FRACTIONS_EXPLORATION_ID_1, RATIOS_EXPLORATION_ID_0,
    RATIOS_EXPLORATION_ID_1, RATIOS_EXPLORATION_ID_2, RATIOS_EXPLORATION_ID_3
  )
}

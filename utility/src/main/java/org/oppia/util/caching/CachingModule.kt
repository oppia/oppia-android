package org.oppia.util.caching

import dagger.Module
import dagger.Provides

private const val FRACTIONS_TOPIC = "GJ2rLXRKD5hw"
private const val RATIOS_TOPIC = "omzF4oqgeTXd"

/** Provides dependencies corresponding to the app's caching policies. */
@Module
class CachingModule {
  @Provides
  @CacheAssetsLocally
  fun provideCacheAssetsLocally(): Boolean = false

  @Provides
  @TopicListToCache
  fun provideTopicListToCache() = listOf(FRACTIONS_TOPIC, RATIOS_TOPIC)
}

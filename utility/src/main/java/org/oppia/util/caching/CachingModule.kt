package org.oppia.util.caching

import dagger.Module
import dagger.Provides

/** Provides dependencies corresponding to the app's caching policies. */
@Module
class CachingModule {
  @Provides
  @CacheAssetsLocally
  fun provideCacheAssetsLocally(): Boolean = false
}

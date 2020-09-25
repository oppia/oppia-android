package org.oppia.android.domain.topic

import dagger.Module
import dagger.Provides
import org.oppia.android.util.caching.CacheAssetsLocally
import javax.inject.Provider

/**
 * Module for providing a [PrimeTopicAssetsController] depending on whether asset caching is enabled
 * (see [CacheAssetsLocally] for specifics).
 */
@Module
class PrimeTopicAssetsControllerModule {
  @Provides
  fun providePrimeTopicAssetsController(
    @CacheAssetsLocally cacheAssetsLocally: Boolean,
    impl: Provider<PrimeTopicAssetsControllerImpl>
  ): PrimeTopicAssetsController {
    return if (cacheAssetsLocally) {
      impl.get()
    } else {
      object : PrimeTopicAssetsController {
        override fun downloadAssets(dialogStyleResId: Int) {
          // Do nothing since caching is disabled.
        }
      }
    }
  }
}

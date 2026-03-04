package org.oppia.android.util.caching.testing

import dagger.Module
import dagger.Provides
import org.oppia.android.util.caching.LoadImagesFromAssets

/**
 * Provides test dependencies corresponding to the app's caching policies. In particular, this
 * module disables caching since most tests do not need to test caching flows.
 */
@Module
class CachingTestModule {
  @Provides
  @LoadImagesFromAssets
  fun provideLoadImagesFromAssets(): Boolean = false
}

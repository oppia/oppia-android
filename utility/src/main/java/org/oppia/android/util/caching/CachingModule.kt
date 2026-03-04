package org.oppia.android.util.caching

import dagger.Module
import dagger.Provides

/** Provides dependencies corresponding to the app's caching policies. */
@Module
class CachingModule {
  @Provides
  @LoadImagesFromAssets
  fun provideLoadImagesFromAssets(): Boolean = false
}

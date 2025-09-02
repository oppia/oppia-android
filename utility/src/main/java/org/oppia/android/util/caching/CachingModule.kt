package org.oppia.android.util.caching

import dagger.Module
import dagger.Provides

/** Provides dependencies corresponding to the app's caching policies. */
@Module
class CachingModule {
  @Provides
  @LoadLessonProtosFromAssets
  fun provideLoadLessonProtosFromAssets(): Boolean = true

  @Provides
  @LoadImagesFromAssets
  fun provideLoadImagesFromAssets(): Boolean = true
}

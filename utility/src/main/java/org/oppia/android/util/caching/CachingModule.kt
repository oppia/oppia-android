package org.oppia.android.util.caching

import dagger.Module
import dagger.Provides
import org.oppia.android.util.platformparameter.PlatformParameterValue

/** Provides dependencies corresponding to the app's caching policies. */
@Module
class CachingModule {
  @Provides
  @LoadLessonProtosFromAssets
  fun provideLoadLessonProtosFromAssets(
    @LoadLessonProtosFromAssets loadLessonProtosFromAssets: PlatformParameterValue<Boolean>
  ): Boolean = loadLessonProtosFromAssets.value

  @Provides
  @LoadImagesFromAssets
  fun provideLoadImagesFromAssets(
    @LoadImagesFromAssets loadImagesFromAssets: PlatformParameterValue<Boolean>
  ): Boolean = loadImagesFromAssets.value
}

package org.oppia.android.util.caching

import dagger.Module
import dagger.Provides

/**
 * Provides dependencies corresponding to the app's caching policies for developer builds.
 *
 * This module is identical to [CachingModule] except that it sets [LoadThumbnailsFromGcs] to
 * false, ensuring that developer builds use local drawable resources for thumbnails instead of
 * attempting to fetch them from Google Cloud Storage (which is inaccessible in dev builds).
 */
@Module
class DevCachingModule {
  @Provides
  @LoadLessonProtosFromAssets
  fun provideLoadLessonProtosFromAssets(): Boolean = false

  @Provides
  @LoadImagesFromAssets
  fun provideLoadImagesFromAssets(): Boolean = false

  @Provides
  @LoadThumbnailsFromGcs
  fun provideLoadThumbnailsFromGcs(): Boolean = false
}

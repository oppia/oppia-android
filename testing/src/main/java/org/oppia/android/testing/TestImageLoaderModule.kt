package org.oppia.android.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.util.image.ImageLoader
import org.oppia.android.util.image.TestGlideImageLoader

/** Provides image loading dependencies for unit tests. */
@Module
abstract class TestImageLoaderModule {
  @Binds
  abstract fun provideFakeImageLoader(impl: TestGlideImageLoader): ImageLoader
}

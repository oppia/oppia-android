package org.oppia.android.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.util.parser.TestGlideImageLoader
import org.oppia.android.util.parser.ImageLoader

/** Provides image loading dependencies for unit tests. */
@Module
abstract class TestImageLoaderModule {
  @Binds
  abstract fun provideFakeImageLoader(impl: TestGlideImageLoader): ImageLoader
}

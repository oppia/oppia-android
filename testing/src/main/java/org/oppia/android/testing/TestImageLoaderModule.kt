package org.oppia.android.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.util.parser.FakeImageLoader
import org.oppia.android.util.parser.ImageLoader

/** Provides image loading dependencies for unit tests. */
@Module
abstract class TestImageLoaderModule {
  @Binds
  abstract fun provideGlideImageLoader(impl: FakeImageLoader): ImageLoader
}

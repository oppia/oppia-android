package org.oppia.util.parser

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides image loading dependencies. */
@Module
class GlideImageLoaderModule {

  @Provides
  @Singleton
  @ImageLoaderAnnotation
  fun providesGlideImageLoader(): ImageLoader {
    return GlideImageLoader()
  }
}

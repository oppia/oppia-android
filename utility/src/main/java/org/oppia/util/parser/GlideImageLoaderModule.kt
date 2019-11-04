package org.oppia.util.parser

import dagger.Binds
import dagger.Module

/** Provides image loading dependencies. */
@Module
abstract class GlideImageLoaderModule {

  @Binds
  @ImageLoaderAnnotation
  abstract fun provideGlideImageLoader(impl: GlideImageLoader): ImageLoader
}

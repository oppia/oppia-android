package org.oppia.android.util.parser.image

import dagger.Binds
import dagger.Module

/** Provides image loading dependencies. */
@Module
abstract class GlideImageLoaderModule {

  @Binds
  abstract fun provideGlideImageLoader(impl: GlideImageLoader): ImageLoader
}

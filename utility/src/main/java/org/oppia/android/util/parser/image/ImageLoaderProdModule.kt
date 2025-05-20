package org.oppia.android.util.parser.image

import dagger.Binds
import dagger.Module

/** Provides image loading dependencies using Glide. */
@Module
abstract class ImageLoaderProdModule {

  @Binds
  abstract fun provideGlideImageLoader(impl: GlideImageLoader): ImageLoader
}

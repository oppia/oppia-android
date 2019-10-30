package org.oppia.util.parser

import dagger.Binds
import dagger.Module

@Module
abstract class GlideImageLoaderModule {
  @Binds
  abstract fun provideGlideImageLoader(impl: GlideImageLoader): ImageLoader
}

package org.oppia.util.parser

import dagger.Module
import dagger.Binds
import dagger.Provides
import javax.inject.Singleton

//@Module
//abstract class ImageLoaderModule {
//
//  @Binds
//  abstract fun bindImageLoader(glideImageLoader: GlideImageLoader): ImageLoader
//}
@Module
class ImageLoaderModule {

  @Provides
  @Singleton
  @ImageLoaderAnnotation
  fun providesGlideImageLoader(): ImageLoader {
    return GlideImageLoader()
  }
}

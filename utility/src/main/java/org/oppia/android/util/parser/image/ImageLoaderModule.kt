package org.oppia.android.util.parser.image

import dagger.Binds
import dagger.Module

/** Module for binding [ImageLoader] to an implementation that relies on real time. */
@Module
interface ImageLoaderModule {
  @Binds
  fun bindImageLoader(impl: TestGlideImageLoader): ImageLoader
}

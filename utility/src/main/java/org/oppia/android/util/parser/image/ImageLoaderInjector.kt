package org.oppia.android.util.parser.image

/** Application level injector for testing utilities. */
interface ImageLoaderInjector {
  /** Returns an [ImageLoader] from the Dagger graph. */
  fun getImageLoader(): ImageLoader
}

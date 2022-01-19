package org.oppia.android.util.parser.image

/** Provider for [ImageLoaderInjector]. */
interface ImageLoaderInjectorProvider {
  /** Returns an [ImageLoaderInjector]. */
  fun getImageLoaderInjector(): ImageLoaderInjector
}

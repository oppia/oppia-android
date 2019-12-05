package org.oppia.util.parser

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides image-extraction URL dependencies. */
@Module
class ImageParsingModule {
  @Provides
  @DefaultGcsPrefix
  @Singleton
  fun provideDefaultGcsPrefix(): String {
    return "https://storage.googleapis.com"
  }

  @Provides
  @ImageDownloadUrlTemplate
  @Singleton
  fun provideImageDownloadUrlTemplate(): String {
    return "%s/%s/assets/image/%s"
  }
}

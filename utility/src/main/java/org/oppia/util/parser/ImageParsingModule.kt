package org.oppia.util.parser

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

/** Provides image-extraction URL dependencies. */
@Module
class ImageParsingModule {
  @Provides
  @DefaultGcsPrefix
  @Singleton
  fun provideDefaultGcsPrefix(): String {
    return "https://storage.googleapis.com/"
  }

  @Provides
  @DefaultGcsResource
  @Singleton
  fun provideDefaultGcsResource(): String {
    return "oppiaserver-resources/"
  }

  @Provides
  @ImageDownloadUrlTemplate
  @Singleton
  fun provideImageDownloadUrlTemplate(): String {
    return "%s/%s/assets/image/%s"
  }
}

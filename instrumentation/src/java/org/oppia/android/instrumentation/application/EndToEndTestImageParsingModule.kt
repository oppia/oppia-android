package org.oppia.android.instrumentation.application

import dagger.Module
import dagger.Provides
import org.oppia.android.util.parser.image.DefaultGcsPrefix
import org.oppia.android.util.parser.image.ImageDownloadUrlTemplate
import org.oppia.android.util.parser.image.ThumbnailDownloadUrlTemplate
import javax.inject.Singleton

/** Provides image-extraction URL dependencies from local dev server. */
@Module
class EndToEndTestImageParsingModule {
  @Provides
  @DefaultGcsPrefix
  @Singleton
  fun provideDefaultGcsPrefix(): String {
    return "http://localhost:8181/"
  }

  @Provides
  @ImageDownloadUrlTemplate
  @Singleton
  fun provideImageDownloadUrlTemplate(): String {
    return "%s/%s/assets/image/%s"
  }

  @Provides
  @ThumbnailDownloadUrlTemplate
  @Singleton
  fun provideThumbnailDownloadUrlTemplate(): String {
    return "%s/%s/assets/thumbnail/%s"
  }
}

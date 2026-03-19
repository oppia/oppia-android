package org.oppia.android.util.parser.image

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Provides developer-only image-extraction URL dependencies.
 *
 * This module replaces [ImageParsingModule] in developer builds. It overrides the default GCS
 * prefix with a `content://` URI scheme that routes image requests to a local
 * [ContentProvider][android.content.ContentProvider] instead of Google Cloud Storage. This allows
 * thumbnail images to be served from local drawable resources in dev builds where GCS assets are
 * inaccessible.
 */
@Module
class DevImageParsingModule {
  @Provides
  @DefaultGcsPrefix
  @Singleton
  fun provideDefaultGcsPrefix(): String {
    return "content://org.oppia.android.provider"
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

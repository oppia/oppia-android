package org.oppia.util.parser

import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/** Provides image-extraction URL dependencies. */
@Module
class ImageParsingModule {
  @Provides
  @Singleton
  fun provide_GCS_PREFIX(): String {
    return "https://storage.googleapis.com/"
  }

  @Provides
  @Singleton
  fun provide_GCS_RESOURCE_BUCKET_NAME(): String {
    return  "oppiaserver-resources/"
  }

  @Provides
  @Singleton
  fun provide_IMAGE_DOWNLOAD_URL_TEMPLATE(): String {
    return "%s/%s/assets/image/%s"
  }
}

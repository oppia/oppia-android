package org.oppia.util.gcsresource

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Provides the default name of the GCS Resource bucket */
@Module
class GcsResourceModule {
  @Provides
  @DefaultResource
  @Singleton
  fun provideDefaultGcsResource(): String {
    return "oppiaserver-resources"
  }
}

package org.oppia.util.gcsresource

import dagger.Module
import dagger.Provides

/** Provides GCS resource bucket names. */
@Module
class GcsResourceModule {
  @Provides
  @DefaultResourceBucketName
  fun provideDefaultGcsResource(): String {
    return "oppiaserver-resources"
  }

  @Provides
  @QuestionResourceBucketName
  fun provideQuestionResourceBucketName(): String {
    return "oppiatestserver-resources"
  }
}

package org.oppia.android.instrumentation

import dagger.Module
import dagger.Provides
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.QuestionResourceBucketName

/** Provides GCS resource bucket names. */
@Module
class TestGcsResourceModule {
  @Provides
  @DefaultResourceBucketName
  fun provideDefaultGcsResource(): String {
    return "assetsdevhandler"
  }

  @Provides
  @QuestionResourceBucketName
  fun provideQuestionResourceBucketName(): String {
    return "assetsdevhandler"
  }
}

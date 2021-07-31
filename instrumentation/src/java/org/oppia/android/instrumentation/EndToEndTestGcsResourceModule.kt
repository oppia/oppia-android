package org.oppia.android.instrumentation

import dagger.Module
import dagger.Provides
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.QuestionResourceBucketName

/** Provides Fake GCS resource of local dev server. */
@Module
class EndToEndTestGcsResourceModule {
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

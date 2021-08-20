package org.oppia.android.instrumentation.application

import dagger.Module
import dagger.Provides
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.QuestionResourceBucketName

/** Provides fake GCS resources for e2e tests (i.e. the local dev server). */
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

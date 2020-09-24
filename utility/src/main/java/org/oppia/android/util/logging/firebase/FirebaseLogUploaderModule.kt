package org.oppia.util.logging.firebase

import dagger.Binds
import dagger.Module
import org.oppia.util.logging.LogUploader

/** Provides Log Uploader related dependencies. */
@Module
interface FirebaseLogUploaderModule {
  @Binds
  fun bindFirebaseLogUploader(firebaseLogUploader: FirebaseLogUploader): LogUploader
}

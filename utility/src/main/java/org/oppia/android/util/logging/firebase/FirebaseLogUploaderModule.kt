package org.oppia.android.util.logging.firebase

import dagger.Binds
import dagger.Module
import org.oppia.android.util.logging.LogUploader

/** Provides Log Uploader related dependencies. */
@Module
interface FirebaseLogUploaderModule {
  @Binds
  fun bindFirebaseLogUploader(firebaseLogUploader: FirebaseLogUploader): LogUploader
}

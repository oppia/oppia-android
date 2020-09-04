package org.oppia.domain.oppialogger.loguploader

import dagger.Binds
import dagger.Module

/** Provides Log Uploader related dependencies. */
@Module
interface FirebaseLogUploaderModule {
  @Binds
  fun bindFirebaseLogUploader(firebaseLogUploader: FirebaseLogUploader): LogUploader
}

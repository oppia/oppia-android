package org.oppia.domain.oppialogger.loguploader

import dagger.Binds
import dagger.Module

/** Provides Log Uploader related dependencies. */
@Module
interface LogUploaderModule {
  @Binds
  fun bindFirebaseLogUploader(firebaseLogUploader: FirebaseLogUploader): LogUploader
}

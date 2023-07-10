package org.oppia.android.util.firestore

import dagger.Binds
import dagger.Module

/** Provides Firestore Uploader related dependencies. */
@Module
interface FirestoreUploaderModule {
  @Binds
  fun bindFirestoreUploader(firestoreUploader: SurveyFirestoreDataUploader): FirestoreDataUploader
}
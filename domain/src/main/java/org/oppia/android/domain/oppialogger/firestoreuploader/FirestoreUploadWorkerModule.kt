package org.oppia.android.domain.oppialogger.firestoreuploader

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.analytics.AnalyticsStartupListener

/** Provides [FirestoreUploadWorker] related dependencies. */
@Module
interface FirestoreUploadWorkerModule {

  @Binds
  @IntoSet
  fun bindFirestoreWorkRequest(
    workManagerInitializer: FirestoreUploadWorkManagerInitializer
  ): AnalyticsStartupListener
}

package org.oppia.android.domain.testing.oppialogger.loguploader

import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.util.logging.LogUploader
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** A test specific fake for the log uploader. */
@Singleton
class FakeLogUploader @Inject constructor() : LogUploader {
  private val eventRequestIdList = mutableListOf<UUID>()
  private val exceptionRequestIdList = mutableListOf<UUID>()

  override fun enqueueWorkRequestForEvents(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    eventRequestIdList.add(workRequest.id)
  }

  override fun enqueueWorkRequestForExceptions(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    exceptionRequestIdList.add(workRequest.id)
  }

  /** Returns the most recent work request id that's stored in the [eventRequestIdList]. */
  fun getMostRecentEventRequestId() = eventRequestIdList.last()

  /** Returns the most recent work request id that's stored in the [exceptionRequestIdList]. */
  fun getMostRecentExceptionRequestId() = exceptionRequestIdList.last()
}

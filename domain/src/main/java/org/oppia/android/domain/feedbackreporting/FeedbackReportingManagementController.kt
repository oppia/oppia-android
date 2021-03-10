package org.oppia.android.domain.feedbackreporting

import org.oppia.android.app.model.FeedbackReportingAppContext
import org.oppia.android.app.model.FeedbackReportingDatabase
import org.oppia.android.app.model.FeedbackReportingDeviceContext
import org.oppia.android.app.model.FeedbackReportingSystemContext
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
import javax.inject.Inject

private const val FEEDBACK_REPORTS_DATABASE_NAME = "feedback_reports_database"

/** Controller for sending feedback reports to remote storage, or saving them on disk. */
class FeedbackReportingManagementController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val networkConnectionUtil: NetworkConnectionUtil
){
  private val feedbackReportDataStore = cacheStoreFactory.create(
    FEEDBACK_REPORTS_DATABASE_NAME, FeedbackReportingDatabase.getDefaultInstance()
  )

  /**
   * Stores a [FeedbackReport] proto in local storage if there is not internet connection, or sends
   * them to remote storage to be processed in the admin dashboard.
   *
   * @param feedbackReportViewModel for the view model representing the feedback provided by the user
   */
  fun submitFeedbackReport(/* FeedbackReport */) {
    val systemContext = getSystemContext()
    val deviceContext = getDeviceContext()
    val appContext = getAppContext()
    if (networkConnectionUtil.getCurrentConnectionStatus() == NONE) {
      // Cache report in local storage
    } else {
      // Push report to remote storage
    }
  }

  private fun getSystemContext(): FeedbackReportingSystemContext {
    return FeedbackReportingSystemContext.newBuilder()
      .build()
  }

  private fun getDeviceContext(): FeedbackReportingDeviceContext {
    return FeedbackReportingDeviceContext.newBuilder()
      .build()
  }

  private fun getAppContext(): FeedbackReportingAppContext {
    return FeedbackReportingAppContext.newBuilder()
      .build()
  }
}
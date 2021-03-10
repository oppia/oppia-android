package org.oppia.android.domain.feedbackreporting

import org.oppia.android.app.model.FeedbackReportingAppContext
import org.oppia.android.app.model.FeedbackReportingDatabase
import org.oppia.android.app.model.FeedbackReportingDeviceContext
import org.oppia.android.app.model.FeedbackReportingSystemContext
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

  fun submitFeedbackReport(/* FeedbackReport */) {
    if (networkConnectionUtil.getCurrentConnectionStatus() == NONE) {
      // Cache report
    } else {

    }
  }

  private fun getSystemContext(): FeedbackReportingSystemContext {
    return FeedbackReportingSystemContext.getDefaultInstance()
  }

  private fun getDeviceContext(): FeedbackReportingDeviceContext {
    val deviceContext = FeedbackReportingDeviceContext.newBuilder()

    return FeedbackReportingDeviceContext.getDefaultInstance()
  }

  private fun getAppContext(): FeedbackReportingAppContext {
    return FeedbackReportingAppContext.getDefaultInstance()
  }
}
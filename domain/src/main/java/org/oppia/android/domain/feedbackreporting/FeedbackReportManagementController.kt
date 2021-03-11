package org.oppia.android.domain.feedbackreporting

import org.oppia.android.app.model.FeedbackReport
import org.oppia.android.app.model.FeedbackReportingAppContext
import org.oppia.android.app.model.FeedbackReportingDatabase
import org.oppia.android.app.model.FeedbackReportingDeviceContext
import org.oppia.android.app.model.FeedbackReportingSystemContext
import org.oppia.android.app.model.UserSuppliedFeedback
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import org.oppia.android.data.backends.gae.model.GaeUserSuppliedFeedback
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

private const val FEEDBACK_REPORT_MANAGEMENT_CONTROLLER_TAG =
  "Feedback Report Management Controller"
private const val FEEDBACK_REPORTS_DATABASE_NAME = "feedback_reports_database"

/** Controller for uploading feedback reports to remote storage or saving them on disk. */
class FeedbackReportManagementController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val feedbackReportingService: FeedbackReportingService,
  private val consoleLogger: ConsoleLogger,
  private val oppiaClock: OppiaClock,
  private val networkConnectionUtil: NetworkConnectionUtil
) {
  private val feedbackReportDataStore = cacheStoreFactory.create(
    FEEDBACK_REPORTS_DATABASE_NAME, FeedbackReportingDatabase.getDefaultInstance()
  )

  /**
   * Stores a [FeedbackReport] proto in local storage if there is not internet connection, or sends
   * them to remote storage to be processed in the admin dashboard.
   *
   * @param feedbackReportViewModel for the view model representing the feedback provided by the user
   */
  fun submitFeedbackReport(feedbackReport: FeedbackReport) {
    if (networkConnectionUtil.getCurrentConnectionStatus() == NONE) {
      storeFeedbackReport(feedbackReport)
    } else {
      uploadFeedbackReport(feedbackReport)
    }
  }

  /**
   * Checks the local disk for any cached feedback reports and uploads them to remote storage.
   * This is called by a [FeedbackReportUploadWorker] when it detects a network connection to send
   * all local reports to remote storage.
   *
   * @return a list of feedback reports to upload to remote storage
   */
  suspend fun getCachedReportsList(): MutableList<FeedbackReport> {
    return feedbackReportDataStore.readDataAsync().await().reportsList
  }

  /**
   * Sends an individual [FeedbackReport] to remote storage.
   *
   * @param report a [FeedbackReport] to upload
   */
  fun uploadFeedbackReport(report: FeedbackReport) {
    val gaeUserSuppliedFeedback = createGaeUserSupppliedFeedback(report.userSuppliedFeedback)
    val gaeFeedbackReport = GaeFeedbackReport(
      reportCreationTimestampMs = report.reportCreationTimestampMs,
      userSuppliedFeedback = getUserSuppliedInfo(),

    )
    feedbackReportingService.postFeedbackReport(report = gaeFeedbackReport)

  }

  /** Removes the first cached report from the store. */
  fun removeFirstCachedReport() {
    feedbackReportDataStore.storeDataAsync(updateInMemoryCache = true) { feedbackReportDatabase ->
      return@storeDataAsync feedbackReportDatabase.toBuilder().removeReports(0).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          FEEDBACK_REPORT_MANAGEMENT_CONTROLLER_TAG,
          "Failed to remove report from store",
          it
        )
      }
    }
  }

  private fun storeFeedbackReport(report: FeedbackReport) {
    feedbackReportDataStore.storeDataAsync(updateInMemoryCache = true) { feedbackReportDatabase ->
      return@storeDataAsync feedbackReportDatabase.toBuilder().addReports(report).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          FEEDBACK_REPORT_MANAGEMENT_CONTROLLER_TAG,
          "Failed to store feedback report",
          it
        )
      }
    }
  }

  private fun createGaeUserSuppliedInfo(userSuppliedFeedback: UserSuppliedFeedback): GaeUserSuppliedFeedback {
    return UserSuppliedFeedback.newBuilder()
      .build()
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
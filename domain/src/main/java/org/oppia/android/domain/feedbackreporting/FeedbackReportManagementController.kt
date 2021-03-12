package org.oppia.android.domain.feedbackreporting

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.oppia.android.app.model.FeedbackReport
import org.oppia.android.app.model.FeedbackReportingAppContext
import org.oppia.android.app.model.FeedbackReportingDatabase
import org.oppia.android.app.model.FeedbackReportingDeviceContext
import org.oppia.android.app.model.FeedbackReportingSystemContext
import org.oppia.android.app.model.Issue
import org.oppia.android.app.model.Issue.IssueCategoryCase
import org.oppia.android.app.model.LanguageIssue
import org.oppia.android.app.model.UserSuppliedFeedback
import org.oppia.android.app.model.UserSuppliedFeedback.ReportTypeCase
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingAppContext
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingDeviceContext
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingSystemContext
import org.oppia.android.data.backends.gae.model.GaeUserSuppliedFeedback
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.oppialogger.exceptions.toException
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.EventLogger
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

private const val FEEDBACK_REPORT_MANAGEMENT_CONTROLLER_TAG =
  "Feedback Report Management Controller"
private const val FEEDBACK_REPORTS_DATABASE_NAME = "feedback_reports_database"

/** Controller for uploading feedback reports to remote storage or saving them on disk. */
class FeedbackReportManagementController @Inject constructor(
  private val context: Context,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val feedbackReportingService: FeedbackReportingService,
  private val consoleLogger: ConsoleLogger,
  private val eventLogger: EventLogger,
  private val analyticsController: AnalyticsController,
  private val oppiaClock: OppiaClock,
  private val networkConnectionUtil: NetworkConnectionUtil,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
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
    val gaeFeedbackReport = GaeFeedbackReport(
      reportCreationTimestampMs = report.reportCreationTimestampMs,
      userSuppliedFeedback = createGaeUserSuppliedFeedback(report.userSuppliedInfo),
      systemContext = getSystemContext(report.systemContext),
      deviceContext = getDeviceContext(report.deviceContext),
      appContext = getAppContext(report.appContext)
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

  // Creates the Moshi data class that gets sent in the service for the information collected from
  // user reponses.
  private fun createGaeUserSuppliedFeedback(
    userSuppliedFeedback: UserSuppliedFeedback
  ): GaeUserSuppliedFeedback {
    val reportType = userSuppliedFeedback.reportTypeCase
    when (reportType) {
      ReportTypeCase.SUGGESTION -> {
        val suggestion = userSuppliedFeedback.suggestion
        return GaeUserSuppliedFeedback(
          reportType = reportType.name,
          category = suggestion.suggestionCategory.name,
          feedbackList = null,
          openTextUserInput = suggestion.userSubmittedSuggestion
        )
      }
      ReportTypeCase.ISSUE -> {
        return createGaeUserSuppliedFeedbackForIssue(reportType.name, userSuppliedFeedback.issue)
      }
      ReportTypeCase.CRASH -> {
        val crash = userSuppliedFeedback.crash
        return GaeUserSuppliedFeedback(
          reportType = reportType.name,
          category = crash.crashLocation.name,
          feedbackList = null,
          openTextUserInput = crash.crashExplanation
        )
      }
      else -> throw IllegalArgumentException(
        "Encountered unexpected feedback report type: ${userSuppliedFeedback.reportTypeCase.name}"
      )
    }
  }

  // Helper function that creates a Moshi data class object based on the type of Issue report.
  private fun createGaeUserSuppliedFeedbackForIssue(
    reportTypeName: String,
    issue: Issue
  ): GaeUserSuppliedFeedback {
    var category = issue.issueCategoryCase.name
    var issuesList: List<String>? = null
    var userInput: String? = null
    when (issue.issueCategoryCase) {
      IssueCategoryCase.LESSON_QUESTION_ISSUE -> {
        issuesList = issue.lessonQuestionIssue.userSelectedOptionsList.map { it.name }
        userInput = issue.lessonQuestionIssue.otherUserInput
      }
      IssueCategoryCase.LANGUAGE_ISSUE -> {
        category = issue.languageIssue.languageIssueCategoryCase.name
        when (issue.languageIssue.languageIssueCategoryCase) {
          LanguageIssue.LanguageIssueCategoryCase.AUDIO_LANGUAGE_ISSUE -> {
            issuesList = issue.languageIssue.audioLanguageIssue.userSelectedOptionsList.map {
              it.name
            }
            userInput = issue.languageIssue.audioLanguageIssue.otherUserInput
          }
          LanguageIssue.LanguageIssueCategoryCase.TEXT_LANGUAGE_ISSUE -> {
            issuesList = issue.languageIssue.textLanguageIssue.userSelectedOptionsList.map {
              it.name
            }
            userInput = issue.languageIssue.textLanguageIssue.otherUserInput
          }
          // General language issues will pass through with no user input or options list.
        }
      }
      IssueCategoryCase.TOPICS_ISSUE -> {
        issuesList = issue.topicsIssue.userSelectedOptionsList.map { it.name }
        userInput = issue.topicsIssue.otherUserInput
      }
      IssueCategoryCase.PROFILE_ISSUE -> {
        issuesList = issue.profileIssue.userSelectedOptionsList.map { it.name }
        userInput = issue.profileIssue.otherUserInput
      }
      IssueCategoryCase.OTHER_ISSUE -> {
        userInput = issue.otherIssue.openUserInput
      }
    }
    return GaeUserSuppliedFeedback(
      reportType = reportTypeName,
      category = category,
      feedbackList = issuesList,
      openTextUserInput = userInput
    )
  }

  private fun getSystemContext(
    systemContext: FeedbackReportingSystemContext
  ): GaeFeedbackReportingSystemContext {
    return GaeFeedbackReportingSystemContext(
      packageVersionName = systemContext.packageVersionName,
      packageVersionCode = systemContext.packageVersionCode,
      countryLocale = systemContext.countryLocale,
      languageLocale = systemContext.languageLocale
    )
  }

  private fun getDeviceContext(
    deviceContext: FeedbackReportingDeviceContext
  ): GaeFeedbackReportingDeviceContext {
    return GaeFeedbackReportingDeviceContext(
      deviceModel = deviceContext.deviceModel,
      sdkVersion = deviceContext.sdkVersion,
      deviceBrand = deviceContext.deviceBrand,
      buildFingerprint = deviceContext.buildFingerprint,
      networkType = deviceContext.networkType.name
    )
  }

  private fun getAppContext(
    appContext: FeedbackReportingAppContext
  ): GaeFeedbackReportingAppContext {
    val eventLogs = getEventLogs()
    return GaeFeedbackReportingAppContext(
      entryPoint = appContext.entryPoint.name,
      topicProgress = appContext.topicProgressList,
      textSize = appContext.textSize.name,
      textLang = appContext.textLanguage.name,
      audioLang = appContext.audioLanguage.name,
      downloadAndUpdateOnlyOnWifi = appContext.deviceSettings.allowDownloadAndUpdateOnlyOnWifi,
      automaticallyUpdateTopics = appContext.deviceSettings.automaticallyUpdateTopics,
      isAdmin = appContext.isAdmin,
      eventLogs = eventLogs,
      logcatLogs = getLogcatLogs(),
    )
  }

  private suspend fun getEventLogs(): List<String> {
    return try {
      val eventLogs = analyticsController.getEventLogStoreList()
      eventLogs.let { eventsList ->
        eventsList.map { it.toString() }
      }
    } catch (e: Exception) {
      consoleLogger.e(LogUploadWorker.TAG, "Failed to upload events", e)
      listOf()
    }
  }

  private fun getLogcatLogs(): List<String> {
    return listOf()
  }
}
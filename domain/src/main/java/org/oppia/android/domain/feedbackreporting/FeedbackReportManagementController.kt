package org.oppia.android.domain.feedbackreporting

import androidx.lifecycle.Transformations
import org.oppia.android.app.model.FeedbackReport
import org.oppia.android.app.model.FeedbackReportingAppContext
import org.oppia.android.app.model.FeedbackReportingAppContext.EntryPointCase
import org.oppia.android.app.model.FeedbackReportingDatabase
import org.oppia.android.app.model.FeedbackReportingDeviceContext
import org.oppia.android.app.model.FeedbackReportingSystemContext
import org.oppia.android.app.model.Issue
import org.oppia.android.app.model.Issue.IssueCategoryCase
import org.oppia.android.app.model.LanguageIssue
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.app.model.UserSuppliedFeedback
import org.oppia.android.app.model.UserSuppliedFeedback.ReportTypeCase
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingAppContext
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingDeviceContext
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingEntryPoint
import org.oppia.android.data.backends.gae.model.GaeFeedbackReportingSystemContext
import org.oppia.android.data.backends.gae.model.GaeUserSuppliedFeedback
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
import javax.inject.Inject
import javax.inject.Singleton

private const val FEEDBACK_REPORT_MANAGEMENT_CONTROLLER_TAG =
  "Feedback Report Management Controller"
private const val FEEDBACK_REPORTS_DATABASE_NAME = "feedback_reports_database"

/** Controller for uploading feedback reports to remote storage or saving them on disk. */
@Singleton
class FeedbackReportManagementController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val feedbackReportingService: FeedbackReportingService,
  private val consoleLogger: ConsoleLogger,
  private val analyticsController: AnalyticsController,
  private val networkConnectionUtil: NetworkConnectionUtil,
) {
  private val feedbackReportDataStore = cacheStoreFactory.create(
    FEEDBACK_REPORTS_DATABASE_NAME, FeedbackReportingDatabase.getDefaultInstance()
  )

  private val oppiaEventLogsLiveData by lazy {
    analyticsController.getEventLogStore().toLiveData()
  }

  private val eventsLogListLiveData = Transformations.map(oppiaEventLogsLiveData) {
    it.getOrDefault(OppiaEventLogs.getDefaultInstance())
  }

  private val eventsLogList = Transformations.map(eventsLogListLiveData) { oppiaEventLogs ->
    oppiaEventLogs.eventLogList.map { it.toString() }
  }.value

  /**
   * Submits a [FeedbackReport] to remote storage to be processed in the admin dashboard, or saves it
   * to local storage if there is no internet connection available to send reports.
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
   * Gets any feedback reports saved locally on the device. This is called by a
   * [FeedbackReportUploadWorker] when it detects a network reconnection to send all local reports
   * to remote storage.
   *
   * @return a list of feedback reports to upload to remote storage
   */
  suspend fun getCachedReportsList(): MutableList<FeedbackReport> {
    return feedbackReportDataStore.readDataAsync().await().reportsList
  }

  /**
   * Gets the data store of all locally-saved feedback reports.
   *
   * @return the data provider for the storage database
   */
  fun getFeedbackReportStore(): DataProvider<FeedbackReportingDatabase> {
    return feedbackReportDataStore
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

  // Helper function to store a single report in memory on the device.
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
          otherUserInputText = suggestion.userSubmittedSuggestion
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
          otherUserInputText = crash.crashExplanation
        )
      }
      else -> throw IllegalArgumentException(
        "Encountered unexpected feedback report type: ${userSuppliedFeedback.reportTypeCase.name}"
      )
    }
  }

  // Helper function that construct a Moshi data class object for the information provided by the user,
  // based on the type of Issue report.
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
          // General language issues will pass through with no user input or options list as users
          // aren't presented with specific issues to choose from if they don't specify what type of
          // language issue.
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
      otherUserInputText = userInput
    )
  }

  // Helper function that construct a Moshi data class object for the device's system information.
  private fun getSystemContext(
    systemContext: FeedbackReportingSystemContext
  ): GaeFeedbackReportingSystemContext {
    return GaeFeedbackReportingSystemContext(
      packageVersionName = systemContext.packageVersionName,
      packageVersionCode = systemContext.packageVersionCode,
      countryLocaleCode = systemContext.countryLocaleCode,
      languageLocaleCode = systemContext.languageLocaleCode
    )
  }

  // Helper function that construct a Moshi data class object for the device's build information.
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

  // Helper function that construct a Moshi data class object for the app's information.
  private fun getAppContext(
    appContext: FeedbackReportingAppContext
  ): GaeFeedbackReportingAppContext {
    return GaeFeedbackReportingAppContext(
      entryPoint = getEntryPointData(appContext),
      completedExplorationIds = appContext.completedExplorationIdsList,
      textSize = appContext.textSize.name,
      textLang = appContext.textLanguage.name,
      audioLang = appContext.audioLanguage.name,
      downloadAndUpdateOnlyOnWifi = appContext.deviceSettings.allowDownloadAndUpdateOnlyOnWifi,
      automaticallyUpdateTopics = appContext.deviceSettings.automaticallyUpdateTopics,
      isAdmin = appContext.isAdmin,
      eventLogs = eventsLogList ?: emptyList(),
      logcatLogs = getLogcatLogs(),
    )
  }

  // Helper function that construct a Moshi data class object for the entry-point used by the user.
  private fun getEntryPointData(
    appContext: FeedbackReportingAppContext
  ): GaeFeedbackReportingEntryPoint {
    var topicId: String? = null
    var storyId: String? = null
    var explorationId: String? = null
    var subtopicId: String? = null
    when (appContext.entryPointCase) {
      // Get the current topic information if the report is sent during a lesson or revision session.
      EntryPointCase.LESSON_PLAYER -> {
        val lesson = appContext.lessonPlayer
        topicId = lesson.topicId
        storyId = lesson.storyId
        explorationId = lesson.explorationId
      }
      EntryPointCase.REVISION_CARD -> {
        val revisionCard = appContext.revisionCard
        topicId = revisionCard.topicId
        subtopicId = revisionCard.subtopicId
      }
    }
    return GaeFeedbackReportingEntryPoint(
      entryPointName = appContext.entryPointCase.name,
      topicId = topicId,
      storyId = storyId,
      explorationId = explorationId,
      subtopicId = subtopicId
    )
  }

  // Helper function to retrieve the logcat logs from the device.
  private fun getLogcatLogs(): List<String> {
    return listOf()
  }
}

package org.oppia.android.domain.oppialogger

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.RevisionCardContext
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** Logger that handles general-purpose logging throughout the domain & UI layers. */
class OppiaLogger @Inject constructor(private val consoleLogger: ConsoleLogger) {
  /** Logs a verbose message with the specified tag. See [ConsoleLogger.v] for more context. */
  fun v(tag: String, msg: String) {
    consoleLogger.v(tag, msg)
  }

  /**
   * Logs a verbose message with the specified tag, message and exception. See [ConsoleLogger.v]
   * for more context.
   */
  fun v(tag: String, msg: String, tr: Throwable) {
    consoleLogger.v(tag, msg, tr)
  }

  /** Logs a debug message with the specified tag. See [ConsoleLogger.d] for more context. */
  fun d(tag: String, msg: String) {
    consoleLogger.d(tag, msg)
  }

  /**
   * Logs a debug message with the specified tag, message and exception. See [ConsoleLogger.d] for
   * more context.
   */
  fun d(tag: String, msg: String, tr: Throwable) {
    consoleLogger.d(tag, msg, tr)
  }

  /** Logs an info message with the specified tag. See [ConsoleLogger.i] for more context. */
  fun i(tag: String, msg: String) {
    consoleLogger.i(tag, msg)
  }

  /**
   * Logs an info message with the specified tag, message and exception. See [ConsoleLogger.i] for
   * more context.
   */
  fun i(tag: String, msg: String, tr: Throwable) {
    consoleLogger.i(tag, msg, tr)
  }

  /** Logs a warn message with the specified tag. See [ConsoleLogger.w] for more context. */
  fun w(tag: String, msg: String) {
    consoleLogger.w(tag, msg)
  }

  /**
   * Logs a warn message with the specified tag, message and exception. See [ConsoleLogger.w] for
   * more context.
   */
  fun w(tag: String, msg: String, tr: Throwable) {
    consoleLogger.w(tag, msg, tr)
  }

  /** Logs an error message with the specified tag. See [ConsoleLogger.e] for more context. */
  fun e(tag: String, msg: String) {
    consoleLogger.e(tag, msg)
  }

  /**
   * Logs an error message with the specified tag, message and exception. See [ConsoleLogger.e] for
   * more context.
   */
  fun e(tag: String, msg: String, tr: Throwable?) {
    consoleLogger.e(tag, msg, tr)
  }

  /** Returns the context of the event indicating that the user opened the home activity. */
  fun createOpenHomeContext(): EventLog.Context {
    return EventLog.Context.newBuilder().setOpenHome(true).build()
  }

  /**
   * Returns the context of the event indicating that the user opened the profile chooser activity.
   */
  fun createOpenProfileChooserContext(): EventLog.Context {
    return EventLog.Context.newBuilder().setOpenProfileChooser(true).build()
  }

  /** Returns the context of the event indicating that the user opened the exploration activity. */
  fun createOpenExplorationActivityContext(
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenExplorationActivity(
        EventLog.ExplorationContext.newBuilder()
          .setClassroomId(classroomId)
          .setTopicId(topicId)
          .setStoryId(storyId)
          .setExplorationId(explorationId)
          .build()
      )
      .build()
  }

  /** Returns the context of the event indicating that the user opened the question player. */
  fun createOpenQuestionPlayerContext(
    questionId: String,
    skillId: List<String>
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenQuestionPlayer(
        EventLog.QuestionContext.newBuilder()
          .setQuestionId(questionId)
          .addAllSkillId(skillId)
          .build()
      )
      .build()
  }

  /** Returns the context of the event indicating that the user opened the practice tab. */
  fun createOpenPracticeTabContext(
    topicId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenPracticeTab(
        EventLog.TopicContext.newBuilder()
          .setTopicId(topicId)
          .build()
      )
      .build()
  }

  /** Returns the context of the event indicating that the user opened the info tab. */
  fun createOpenInfoTabContext(
    topicId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenInfoTab(
        EventLog.TopicContext.newBuilder()
          .setTopicId(topicId)
          .build()
      )
      .build()
  }

  /** Returns the context of the event indicating that the user opened the lessons tab. */
  fun createOpenLessonsTabContext(
    topicId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenLessonsTab(
        EventLog.TopicContext.newBuilder()
          .setTopicId(topicId)
          .build()
      )
      .build()
  }

  /** Returns the context of the event indicating that the user opened the revision tab. */
  fun createOpenRevisionTabContext(
    topicId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenRevisionTab(
        EventLog.TopicContext.newBuilder()
          .setTopicId(topicId)
          .build()
      )
      .build()
  }

  /** Returns the context of the event indicating that the user opened the story activity. */
  fun createOpenStoryActivityContext(
    topicId: String,
    storyId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenStoryActivity(
        EventLog.StoryContext.newBuilder()
          .setTopicId(topicId)
          .setStoryId(storyId)
          .build()
      )
      .build()
  }

  /** Returns the context of the event indicating that the user opened the concept card. */
  fun createOpenConceptCardContext(
    skillId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenConceptCard(
        EventLog.ConceptCardContext.newBuilder()
          .setSkillId(skillId)
          .build()
      )
      .build()
  }

  /** Returns the context of the event indicating that the user opened a revision card. */
  fun createOpenRevisionCardContext(topicId: String, subtopicIndex: Int): EventLog.Context {
    return EventLog.Context.newBuilder().apply {
      openRevisionCard = createRevisionCardContext(topicId, subtopicIndex)
    }.build()
  }

  /** Returns the context of the event indicating that the user closed a revision card. */
  fun createCloseRevisionCardContext(topicId: String, subtopicIndex: Int): EventLog.Context {
    return EventLog.Context.newBuilder().apply {
      closeRevisionCard = createRevisionCardContext(topicId, subtopicIndex)
    }.build()
  }

  private fun createRevisionCardContext(topicId: String, subtopicIndex: Int): RevisionCardContext {
    return RevisionCardContext.newBuilder().apply {
      this.topicId = topicId
      this.subTopicId = subtopicIndex
    }.build()
  }

  /** Returns the context of the event indicating that the user saw the survey popup dialog. */
  fun createShowSurveyPopupContext(
    explorationId: String,
    topicId: String,
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setShowSurveyPopup(
        EventLog.SurveyContext.newBuilder()
          .setExplorationId(explorationId)
          .setTopicId(topicId)
          .build()
      )
      .build()
  }

  /** Returns the context of the event indicating that the user began a survey session. */
  fun createBeginSurveyContext(
    explorationId: String,
    topicId: String,
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setBeginSurvey(
        EventLog.SurveyContext.newBuilder()
          .setExplorationId(explorationId)
          .setTopicId(topicId)
          .build()
      )
      .build()
  }

  /**
   * Returns the context of the event indicating that the user completed app onboarding for the
   * first time.
   */
  fun createAppOnBoardingContext(): EventLog.Context {
    return EventLog.Context.newBuilder().setCompleteAppOnboarding(
      EventLog.CompleteAppOnboardingContext.newBuilder()
        .setCompleteAppOnboarding(true)
        .build()
    ).build()
  }

  /** Returns the context of the event indicating that a profile started onboarding. */
  fun createProfileOnboardingStartedContext(profileId: ProfileId): EventLog.Context {
    return EventLog.Context.newBuilder().setStartProfileOnboardingEvent(
      EventLog.ProfileOnboardingContext.newBuilder()
        .setProfileId(profileId)
        .build()
    ).build()
  }

  /** Returns the context of the event indicating that a profile completed onboarding. */
  fun createProfileOnboardingEndedContext(profileId: ProfileId): EventLog.Context {
    return EventLog.Context.newBuilder().setEndProfileOnboardingEvent(
      EventLog.ProfileOnboardingContext.newBuilder()
        .setProfileId(profileId)
        .build()
    ).build()
  }

  /**
   * Returns the context of the event indicating that a console error was logged.
   */
  fun createConsoleLogContext(
    logLevel: String,
    logTag: String,
    errorLog: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder().setConsoleLog(
      EventLog.ConsoleLoggerContext.newBuilder()
        .setLogLevel(logLevel)
        .setLogTag(logTag)
        .setFullErrorLog(errorLog)
        .build()
    ).build()
  }

  /**
   * Returns the context of the event indicating that a retrofit call was made.
   */
  fun createRetrofitCallContext(
    url: String,
    headers: String,
    body: String,
    responseCode: Int
  ): EventLog.Context {
    return EventLog.Context.newBuilder().setRetrofitCallContext(
      EventLog.RetrofitCallContext.newBuilder()
        .setRequestUrl(url)
        .setHeaders(headers)
        .setBody(body)
        .setResponseStatusCode(responseCode)
        .build()
    ).build()
  }

  /**
   * Returns the context of the event indicating that a retrofit call failed.
   */
  fun createRetrofitCallFailedContext(
    url: String,
    headers: String,
    body: String,
    responseCode: Int,
    errorMessage: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder().setRetrofitCallFailedContext(
      EventLog.RetrofitCallFailedContext.newBuilder()
        .setRequestUrl(url)
        .setHeaders(headers)
        .setBody(body)
        .setResponseStatusCode(responseCode)
        .setErrorMessage(errorMessage)
        .build()
    ).build()
  }

  /**
   * Returns the context of the event indicating the amount of time spent with the app in
   * foreground.
   */
  fun createAppInForegroundTimeContext(
    installationId: String?,
    appSessionId: String,
    foregroundTime: Long
  ): EventLog.Context {
    return EventLog.Context.newBuilder().setAppInForegroundTime(
      EventLog.AppInForegroundTimeContext.newBuilder()
        .setInstallationId(installationId)
        .setAppSessionId(appSessionId)
        .setForegroundTime(foregroundTime.toFloat())
        .build()
    ).build()
  }
}

package org.oppia.android.domain.oppialogger

import org.oppia.android.app.model.EventLog
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** Logger that handles event logging. */
class OppiaLogger @Inject constructor(
  private val analyticsController: AnalyticsController,
  private val consoleLogger: ConsoleLogger
) {
  /** Logs transition events. See [AnalyticsController.logTransitionEvent] for more context. */
  fun logTransitionEvent(
    timestamp: Long,
    eventContext: EventLog.Context
  ) {
    analyticsController.logTransitionEvent(timestamp, eventContext)
  }

  /** Logs click events. See [AnalyticsController.logClickEvent] for more context. */
  fun logClickEvent(
    timestamp: Long,
    eventContext: EventLog.Context
  ) {
    analyticsController.logClickEvent(timestamp, eventContext)
  }

  /** Logs a verbose message with the specified tag. See [ConsoleLogger.v] for more context */
  fun v(tag: String, msg: String) {
    consoleLogger.v(tag, msg)
  }

  /**
   * Logs a verbose message with the specified tag, message and exception. See [ConsoleLogger.v]
   * for more context
   */
  fun v(tag: String, msg: String, tr: Throwable) {
    consoleLogger.v(tag, msg, tr)
  }

  /** Logs a debug message with the specified tag. See [ConsoleLogger.d] for more context */
  fun d(tag: String, msg: String) {
    consoleLogger.d(tag, msg)
  }

  /**
   * Logs a debug message with the specified tag, message and exception. See [ConsoleLogger.d] for
   * more context
   */
  fun d(tag: String, msg: String, tr: Throwable) {
    consoleLogger.d(tag, msg, tr)
  }

  /** Logs an info message with the specified tag. See [ConsoleLogger.i] for more context */
  fun i(tag: String, msg: String) {
    consoleLogger.i(tag, msg)
  }

  /**
   * Logs an info message with the specified tag, message and exception. See [ConsoleLogger.i] for
   * more context
   */
  fun i(tag: String, msg: String, tr: Throwable) {
    consoleLogger.i(tag, msg, tr)
  }

  /** Logs a warn message with the specified tag. See [ConsoleLogger.w] for more context */
  fun w(tag: String, msg: String) {
    consoleLogger.w(tag, msg)
  }

  /**
   * Logs a warn message with the specified tag, message and exception. See [ConsoleLogger.w] for
   * more context
   */
  fun w(tag: String, msg: String, tr: Throwable) {
    consoleLogger.w(tag, msg, tr)
  }

  /** Logs an error message with the specified tag. See [ConsoleLogger.e] for more context */
  fun e(tag: String, msg: String) {
    consoleLogger.e(tag, msg)
  }

  /**
   * Logs an error message with the specified tag, message and exception. See [ConsoleLogger.e] for
   * more context
   */
  fun e(tag: String, msg: String, tr: Throwable?) {
    consoleLogger.e(tag, msg, tr)
  }

  /** Returns the context of the event indicating that the user opened the home activity. */
  fun createOpenHomeContext(): EventLog.Context {
    return EventLog.Context.newBuilder().setOpenHome(true).build()
  }

  /** Returns the context of the event indicating that the user opened the profile chooser activity. */
  fun createOpenProfileChooserContext(): EventLog.Context {
    return EventLog.Context.newBuilder().setOpenProfileChooser(true).build()
  }

  /** Returns the context of the event indicating that the user opened the exploration activity. */
  fun createOpenExplorationActivityContext(
    topicId: String,
    storyId: String,
    explorationId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenExplorationActivity(
        EventLog.ExplorationContext.newBuilder()
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

  /** Returns the context of the event indicating that the user opened the revision card. */
  fun createOpenRevisionCardContext(
    topicId: String,
    subTopicId: Int
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOpenRevisionCard(
        EventLog.RevisionCardContext.newBuilder()
          .setTopicId(topicId)
          .setSubTopicId(subTopicId)
          .build()
      )
      .build()
  }
}

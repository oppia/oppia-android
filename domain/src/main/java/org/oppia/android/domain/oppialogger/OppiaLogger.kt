package org.oppia.android.domain.oppialogger

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.EventAction
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** Logger that handles event logging. */
class OppiaLogger @Inject constructor(
  private val analyticsController: AnalyticsController,
  private val exceptionController: ExceptionsController,
  private val Logger: ConsoleLogger
) {
  /** Logs transition events. See [AnalyticsController.logTransitionEvent] for more context. */
  fun logTransitionEvent(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?
  ) {
    analyticsController.logTransitionEvent(timestamp, eventAction, eventContext)
  }

  /** Logs click events. See [AnalyticsController.logClickEvent] for more context. */
  fun logClickEvent(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?
  ) {
    analyticsController.logClickEvent(timestamp, eventAction, eventContext)
  }

  /** Returns the context of an event related to exploration. */
  fun createExplorationContext(
    topicId: String,
    storyId: String,
    explorationId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setExplorationContext(
        EventLog.ExplorationContext.newBuilder()
          .setTopicId(topicId)
          .setStoryId(storyId)
          .setExplorationId(explorationId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to question. */
  fun createQuestionContext(
    questionId: String,
    skillId: List<String>
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setQuestionContext(
        EventLog.QuestionContext.newBuilder()
          .setQuestionId(questionId)
          .addAllSkillId(skillId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to topic. */
  fun createTopicContext(
    topicId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setTopicContext(
        EventLog.TopicContext.newBuilder()
          .setTopicId(topicId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to story. */
  fun createStoryContext(
    topicId: String,
    storyId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setStoryContext(
        EventLog.StoryContext.newBuilder()
          .setTopicId(topicId)
          .setStoryId(storyId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to concept card. */
  fun createConceptCardContext(
    skillId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setConceptCardContext(
        EventLog.ConceptCardContext.newBuilder()
          .setSkillId(skillId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to revision card. */
  fun createRevisionCardContext(
    topicId: String,
    subTopicId: Int
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setRevisionCardContext(
        EventLog.RevisionCardContext.newBuilder()
          .setTopicId(topicId)
          .setSubTopicId(subTopicId)
          .build()
      )
      .build()
  }

  /**Logs non-fatal exception. See [ExceptionsController.logNonFatalException] for more context.*/
  fun logNonFatalException(exception: Exception, timestampInMillis: Long) {
    exceptionController.logNonFatalException(exception, timestampInMillis)
  }

  /**Logs exception. See [ExceptionsController.logNonFatalException] for more context.*/
  fun logFatalException(exception: Exception, timestampInMillis: Long) {
    exceptionController.logFatalException(exception, timestampInMillis)
  }

  /** Logs a verbose message with the specified tag and message.*/
  fun v(tag: String, msg: String) {
    Logger.v(tag, msg)
  }

  /** Logs a verbose message with the specified tag, message and exception.*/
  fun v(tag: String, msg: String, tr: Throwable) {
    Logger.v(tag, msg, tr)
  }

  /** Logs a debug message with the specified tag*/
  fun d(tag: String, msg: String) {
    Logger.d(tag, msg)
  }

  /** Logs a debug message with the specified tag, message and exception.*/
  fun d(tag: String, msg: String, tr: Throwable) {
    Logger.d(tag, msg, tr)
  }

  /** Logs a info message with the specified tag.*/
  fun i(tag: String, msg: String) {
    Logger.i(tag, msg)
  }

  /** Logs a info message with the specified tag, message and exception.*/
  fun i(tag: String, msg: String, tr: Throwable) {
    Logger.i(tag, msg, tr)
  }

  /** Logs a warn message with the specified tag.*/
  fun w(tag: String, msg: String) {
    Logger.w(tag, msg)
  }

  /** Logs a warn message with the specified tag, message and exception.*/
  fun w(tag: String, msg: String, tr: Throwable) {
    Logger.w(tag, msg, tr)
  }

  /** Logs a error message with the specified tag.*/
  fun e(tag: String, msg: String) {
    Logger.e(tag, msg)
  }

  /** Logs a error message with the specified tag, message and exception.*/
  fun e(tag: String, msg: String, tr: Throwable?) {
    Logger.e(tag, msg, tr)
  }
}

package org.oppia.domain.analytics

import android.content.Context
import org.oppia.app.model.EventLog
import org.oppia.app.model.EventLog.EventAction
import org.oppia.app.model.EventLog.Priority
import org.oppia.util.logging.EventLogger
import javax.inject.Inject

const val TEST_TIMESTAMP = 1556094120000
const val TEST_TOPIC_ID = "test_topicId"
const val TEST_STORY_ID = "test_storyId"
const val TEST_EXPLORATION_ID = "test_explorationId"
const val TEST_QUESTION_ID = "test_questionId"

/**
 * Controller for handling analytics event logging.
 * [logTransitionEvent] should be used to log screen transition events.
 * [logClickEvent] should be used to log button click events.
 */
class AnalyticsController @Inject constructor(
  private val eventLogger: EventLogger
) {
  /**
   * Logs transition events.
   * These events are given HIGH priority.
   */
  fun logTransitionEvent(
    context: Context,
    timestamp: Long,
    eventAction: EventAction,
    explorationContext: EventLog.ExplorationContext?,
    questionContext: EventLog.QuestionContext?
  ) {
    eventLogger.logEvent(
      context,
      createEventLog(
        timestamp, eventAction, explorationContext, questionContext, Priority.ESSENTIAL
      )
    )
  }

  /**
   * Logs click events.
   * These events are given LOW priority.
   */
  fun logClickEvent(
    context: Context,
    timestamp: Long,
    eventAction: EventAction,
    explorationContext: EventLog.ExplorationContext?,
    questionContext: EventLog.QuestionContext?
  ) {
    eventLogger.logEvent(
      context,
      createEventLog(
        timestamp, eventAction, explorationContext, questionContext, Priority.OPTIONAL
      )
    )
  }

  /** Returns an event log containing relevant data for event reporting. */
  private fun createEventLog(
    timestamp: Long,
    eventAction: EventAction,
    explorationContext: EventLog.ExplorationContext?,
    questionContext: EventLog.QuestionContext?,
    priority: Priority
  ): EventLog {

    val event : EventLog.Builder = EventLog.newBuilder()
    event.timestamp = timestamp
    event.actionName = eventAction
    event.priority = priority

    if(explorationContext != null || questionContext != null )
      event.context = contextCreation(explorationContext, questionContext)

    return event.build()
  }

  /** Returns the activity context of an event. */
  private fun contextCreation(
    explorationContext: EventLog.ExplorationContext?,
    questionContext: EventLog.QuestionContext?
  ): EventLog.Context {
    return if (questionContext != null) {
      EventLog.Context.newBuilder()
        .setQuestionContext(questionContext)
        .build()
    } else {
      EventLog.Context.newBuilder()
        .setExplorationContext(explorationContext)
        .build()
    }
  }

  /** Returns the context of an event related to exploration. */
  fun explorationContext(
    topicId: String?,
    storyId: String?,
    explorationId: String?
  ): EventLog.ExplorationContext? {
    return EventLog.ExplorationContext.newBuilder()
      .setTopicId(topicId)
      .setStoryId(storyId)
      .setExplorationId(explorationId)
      .build()
  }

  /** Returns the context of an event related to question. */
  fun questionContext(
    topicId: String?,
    questionId: String?
  ): EventLog.QuestionContext? {
    return EventLog.QuestionContext.newBuilder()
      .setTopicId(topicId)
      .setQuestionId(questionId)
      .build()
  }
}

package org.oppia.domain.analytics

import android.content.Context
import org.oppia.app.model.EventLog
import javax.inject.Inject
import org.oppia.app.model.EventLog.EventAction
import org.oppia.app.model.EventLog.Priority
import org.oppia.util.logging.EventLogger

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
    topicId: String?,
    storyId: String?,
    explorationId: String?,
    questionId: String?
  ) {
    eventLogger.logEvent(
      context,
      createEventLog(
        timestamp, topicId, eventAction, storyId, explorationId, questionId, Priority.ESSENTIAL
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
    topicId: String?,
    storyId: String?,
    explorationId: String?,
    questionId: String?
  ) {
    eventLogger.logEvent(
      context,
      createEventLog(
        timestamp, topicId, eventAction, storyId, explorationId, questionId, Priority.OPTIONAL
      )
    )
  }

  /** Returns an event log containing relevant data for event reporting. */
  private fun createEventLog(
    timestamp: Long,
    topicId: String?,
    eventAction: EventAction,
    storyId: String?,
    explorationId: String?,
    questionId: String?,
    priority: Priority
  ): EventLog {

    return EventLog.newBuilder()
      .setTimestamp(timestamp)
      .setActionName(eventAction)
      .setPriority(priority)
      .setContext(contextCreation(topicId, storyId, explorationId, questionId))
      .build()
  }

  /** Returns the activity context of an event. */
  private fun contextCreation(
    topicId: String?,
    storyId: String?,
    explorationId: String?,
    questionId: String?
  ): EventLog.Context{
    return if(questionId != null){
      EventLog.Context.newBuilder()
        .setQuestionContext(questionContext(topicId, questionId))
        .build()
    } else{
      EventLog.Context.newBuilder()
        .setExplorationContext(explorationContext(topicId, storyId, explorationId))
        .build()
    }
  }

  /** Returns the context of an event related to exploration. */
  private fun explorationContext(
    topicId: String?,
    storyId: String?,
    explorationId: String?
  ): EventLog.Context.ExplorationContext{
    return EventLog.Context.ExplorationContext.newBuilder()
      .setTopicId(topicId)
      .setStoryId(storyId)
      .setExplorationId(explorationId)
      .build()
  }

  /** Returns the context of an event related to question. */
  private fun questionContext(
    topicId: String?,
    questionId: String?
  ): EventLog.Context.QuestionContext{
    return EventLog.Context.QuestionContext.newBuilder()
      .setTopicId(topicId)
      .setQuestionId(questionId)
      .build()
  }
}

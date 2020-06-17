package org.oppia.domain.analytics

import android.content.Context
import org.oppia.app.model.EventLog
import org.oppia.app.model.EventLog.EventAction
import org.oppia.app.model.EventLog.Priority
import org.oppia.util.logging.EventLogger
import javax.inject.Inject

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
    eventContext: EventLog.Context?
  ) {
    eventLogger.logEvent(
      context,
      createEventLog(
        timestamp,
        eventAction,
        eventContext,
        Priority.ESSENTIAL
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
    eventContext: EventLog.Context?
  ) {
    eventLogger.logEvent(
      context,
      createEventLog(
        timestamp,
        eventAction,
        eventContext,
        Priority.OPTIONAL
      )
    )
  }

  /** Returns an event log containing relevant data for event reporting. */
  private fun createEventLog(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?,
    priority: Priority
  ): EventLog {
    val event: EventLog.Builder = EventLog.newBuilder()
    event.timestamp = timestamp
    event.actionName = eventAction
    event.priority = priority

    if (eventContext != null)
      event.context = eventContext

    return event.build()
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
    topicId: String,
    questionId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setQuestionContext(
        EventLog.QuestionContext.newBuilder()
          .setTopicId(topicId)
          .setQuestionId(questionId)
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

  /** Returns the context of an event related to topic. */
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
}

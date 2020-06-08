package org.oppia.domain.analytics

import android.content.Context
import android.os.Bundle
import javax.inject.Inject
import org.oppia.app.model.EventAction
import org.oppia.app.model.Priority
import org.oppia.util.firebase.EventLogger

const val TIMESTAMP_KEY = "timestamp"
const val TOPIC_ID_KEY = "topicId"
const val STORY_ID_KEY = "storyId"
const val QUESTION_ID_KEY = "questionId"
const val EXPLORATION_ID_KEY = "explorationId"
const val PRIORITY_KEY = "priority"
const val TEST_TIMESTAMP = 1556094120000
const val TEST_TOPIC_ID = "test_topicId"
const val TEST_STORY_ID = "test_storyId"
const val TEST_EXPLORATION_ID = "test_explorationId"
const val TEST_QUESTION_ID = "test_questionId"

/**
 * Controller for handling analytics event logging.
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
    eventLogger
      .logEvent(
        context,
        createEventBundle(timestamp, topicId, storyId, explorationId, questionId, Priority.ESSENTIAL),
        eventAction.toString()
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
    eventLogger
      .logEvent(
        context,
        createEventBundle(timestamp, topicId, storyId, explorationId, questionId, Priority.OPTIONAL),
        eventAction.toString()
      )
  }

  /**
   * Returns a bundle containing relevant data for event reporting to console.
   */
  private fun createEventBundle(
    timestamp: Long,
    topicId: String?,
    storyId: String?,
    explorationId: String?,
    questionId: String?,
    priority: Priority
  ): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, timestamp)
    bundle.putString(TOPIC_ID_KEY, topicId)
    bundle.putString(STORY_ID_KEY, storyId)
    bundle.putString(EXPLORATION_ID_KEY, explorationId)
    bundle.putString(QUESTION_ID_KEY, questionId)
    bundle.putString(PRIORITY_KEY, priority.toString())
    return bundle
  }
}

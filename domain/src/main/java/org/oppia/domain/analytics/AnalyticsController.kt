package org.oppia.domain.analytics

import android.content.Context
import android.os.Bundle
import javax.inject.Inject
import org.oppia.app.model.EventAction
import org.oppia.app.model.Priority
import org.oppia.util.firebase.EventLogger

private const val TIMESTAMP = "timestamp"
private const val TOPIC_ID = "topicId"
private const val STORY_ID = "storyId"
private const val QUESTION_ID = "questionId"
private const val EXPLORATION_ID = "explorationId"
private const val PRIORITY = "priority"

/**
 * Controller for handling analytics event logging.
 */
class AnalyticsController @Inject constructor(
  private val eventLogger: EventLogger
) {
  private var bundle: Bundle = Bundle()

  /**
   * Logs transition events.
   * These events are given HIGH priority .
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
    bundle = eventBundleMaker(timestamp, topicId, storyId, explorationId, questionId, Priority.ESSENTIAL)
    eventLogger.logEvent(context, bundle, eventAction.toString())
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
    bundle = eventBundleMaker(timestamp, topicId, storyId, explorationId, questionId, Priority.OPTIONAL)
    eventLogger.logEvent(context, bundle, eventAction.toString())
  }

  /**
   * Returns a bundle containing relevant data for event reporting to console.
   */
  private fun eventBundleMaker(
    timestamp: Long,
    topicId: String?,
    storyId: String?,
    explorationId: String?,
    questionId: String?,
    priority: Priority
  ): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP, timestamp)
    bundle.putString(TOPIC_ID, topicId)
    bundle.putString(STORY_ID, storyId)
    bundle.putString(EXPLORATION_ID, explorationId)
    bundle.putString(QUESTION_ID, questionId)
    bundle.putString(PRIORITY, priority.toString())
    return bundle
  }
}

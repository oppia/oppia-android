package org.oppia.util.logging.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.oppia.app.model.EventLog
import org.oppia.util.logging.EventLogger
import javax.inject.Singleton

const val TIMESTAMP_KEY = "timestamp"
const val TOPIC_ID_KEY = "topicId"
const val STORY_ID_KEY = "storyId"
const val QUESTION_ID_KEY = "questionId"
const val EXPLORATION_ID_KEY = "explorationId"
const val PRIORITY_KEY = "priority"

/** Logger for event logging to Firebase Analytics. */
@Singleton
class FirebaseEventLogger(
  private val firebaseAnalytics: FirebaseAnalytics
) : EventLogger {

  private lateinit var bundle: Bundle

  /** Logs an event to Firebase Analytics. */
  override fun logEvent(context: Context, eventLog: EventLog) {
    bundle =
      when (eventLog.context.activityContextCase.number) {
        1 -> createExplorationContextBundle(eventLog)
        2 -> createQuestionContextBundle(eventLog)
        else -> defaultBundle(eventLog)
      }
    firebaseAnalytics.logEvent(eventLog.actionName.toString(), bundle)
  }

  private fun createExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.explorationContext.topicId)
    bundle.putString(STORY_ID_KEY, eventLog.context.explorationContext.storyId)
    bundle.putString(EXPLORATION_ID_KEY, eventLog.context.explorationContext.explorationId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  private fun createQuestionContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.questionContext.topicId)
    bundle.putString(QUESTION_ID_KEY, eventLog.context.questionContext.questionId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  private fun defaultBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }
}

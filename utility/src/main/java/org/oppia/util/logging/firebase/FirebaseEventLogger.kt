package org.oppia.util.logging.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.oppia.app.model.EventLog
import org.oppia.app.model.EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.QUESTION_CONTEXT
import org.oppia.util.logging.EventBundleCreator
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
) : EventLogger, EventBundleCreator {

  private var bundle = Bundle()

  /** Logs an event to Firebase Analytics. */
  override fun logEvent(context: Context, eventLog: EventLog) {
    bundle =
      when (eventLog.context.activityContextCase) {
        EXPLORATION_CONTEXT -> createExplorationContextBundle(eventLog)
        QUESTION_CONTEXT -> createQuestionContextBundle(eventLog)
        else -> defaultBundle(eventLog)
      }
    firebaseAnalytics.logEvent(eventLog.actionName.toString(), bundle)
  }

  override fun createExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.explorationContext.topicId)
    bundle.putString(STORY_ID_KEY, eventLog.context.explorationContext.storyId)
    bundle.putString(EXPLORATION_ID_KEY, eventLog.context.explorationContext.explorationId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  override fun createQuestionContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.questionContext.topicId)
    bundle.putString(QUESTION_ID_KEY, eventLog.context.questionContext.questionId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  override fun defaultBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }
}

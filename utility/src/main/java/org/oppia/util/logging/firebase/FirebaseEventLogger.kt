package org.oppia.util.logging.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.oppia.app.model.EventLog
import org.oppia.app.model.EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.QUESTION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.STORY_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.TOPIC_CONTEXT
import org.oppia.util.logging.EventBundleCreator
import org.oppia.util.logging.EventLogger
import javax.inject.Singleton

/** Logger for event logging to Firebase Analytics. */
@Singleton
class FirebaseEventLogger(
  private val firebaseAnalytics: FirebaseAnalytics
) : EventLogger {
  private var bundle = Bundle()

  /** Logs an event to Firebase Analytics. */
  override fun logEvent(context: Context, eventLog: EventLog) {
    bundle =
      when (eventLog.context.activityContextCase) {
        EXPLORATION_CONTEXT -> EventBundleCreator().createExplorationContextBundle(eventLog)
        QUESTION_CONTEXT -> EventBundleCreator().createQuestionContextBundle(eventLog)
        STORY_CONTEXT -> EventBundleCreator().createStoryContextBundle(eventLog)
        TOPIC_CONTEXT -> EventBundleCreator().createTopicContextBundle(eventLog)
        else -> EventBundleCreator().defaultBundle(eventLog)
      }
    firebaseAnalytics.logEvent(eventLog.actionName.toString(), bundle)
  }
}

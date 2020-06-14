package org.oppia.testing

import android.content.Context
import android.os.Bundle
import org.oppia.app.model.EventLog
import org.oppia.util.logging.EventBundleCreator
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.firebase.EXPLORATION_ID_KEY
import org.oppia.util.logging.firebase.PRIORITY_KEY
import org.oppia.util.logging.firebase.QUESTION_ID_KEY
import org.oppia.util.logging.firebase.STORY_ID_KEY
import org.oppia.util.logging.firebase.TIMESTAMP_KEY
import org.oppia.util.logging.firebase.TOPIC_ID_KEY
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**  A test specific fake for the event logger. */
@Singleton
class FakeEventLogger @Inject constructor() : EventLogger, EventBundleCreator {
  private val eventList = ArrayList<EventLog>()
  private var bundle = Bundle()

  override fun logEvent(context: Context, eventLog: EventLog) {
    eventList.add(eventLog)
    bundle =
      when (eventLog.context.activityContextCase) {
        EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT ->
          createExplorationContextBundle(eventLog)
        EventLog.Context.ActivityContextCase.QUESTION_CONTEXT ->
          createQuestionContextBundle(eventLog)
        else -> defaultBundle(eventLog)
      }
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

  /** Returns the most recently logged event. */
  fun getMostRecentEvent(): EventLog = eventList.last()

  /** Clears all the events that are currently logged.. */
  fun clearAllEvents() = eventList.clear()

  /** Checks if a certain event has been logged or not. */
  fun hasEventLogged(eventLog: EventLog): Boolean = eventList.contains(eventLog)

  /** Returns true if there are no events logged. */
  fun noEventsPresent(): Boolean = eventList.isEmpty()

  /** Returns the most recently logged event bundle. */
  fun getMostRecentEventBundle(): Bundle = bundle

}
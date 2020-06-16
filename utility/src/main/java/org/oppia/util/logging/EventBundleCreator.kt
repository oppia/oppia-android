package org.oppia.util.logging

import android.os.Bundle
import org.oppia.app.model.EventLog

const val TIMESTAMP_KEY = "timestamp"
const val TOPIC_ID_KEY = "topicId"
const val STORY_ID_KEY = "storyId"
const val QUESTION_ID_KEY = "questionId"
const val EXPLORATION_ID_KEY = "explorationId"
const val PRIORITY_KEY = "priority"

/**
 * Utility for creating bundles from [EventLog] objects.
 * Note that this utility may later upload them to remote services.
 */
open class EventBundleCreator {
  private var bundle = Bundle()

  open fun assignBundleValue(eventLog: EventLog): Bundle {
    bundle =
      when (eventLog.context.activityContextCase) {
        EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT ->
          createExplorationContextBundle(eventLog)
        EventLog.Context.ActivityContextCase.QUESTION_CONTEXT ->
          createQuestionContextBundle(eventLog)
        EventLog.Context.ActivityContextCase.STORY_CONTEXT ->
          createStoryContextBundle(eventLog)
        EventLog.Context.ActivityContextCase.TOPIC_CONTEXT ->
          createTopicContextBundle(eventLog)
        EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET ->
          createNoContextBundle(eventLog)
      }
    return bundle
  }

  /** Creates a bundle from event having exploration context. */
  open fun createExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.explorationContext.topicId)
    bundle.putString(STORY_ID_KEY, eventLog.context.explorationContext.storyId)
    bundle.putString(EXPLORATION_ID_KEY, eventLog.context.explorationContext.explorationId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Creates a bundle from event having question context. */
  open fun createQuestionContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.questionContext.topicId)
    bundle.putString(QUESTION_ID_KEY, eventLog.context.questionContext.questionId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Creates a bundle from event having question context. */
  open fun createTopicContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.topicContext.topicId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Creates a bundle from event having question context. */
  open fun createStoryContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.storyContext.topicId)
    bundle.putString(STORY_ID_KEY, eventLog.context.storyContext.storyId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Creates a bundle from event having no context. */
  open fun createNoContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }
}

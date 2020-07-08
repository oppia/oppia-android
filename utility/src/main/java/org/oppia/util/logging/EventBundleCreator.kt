package org.oppia.util.logging

import android.os.Bundle
import org.oppia.app.model.EventLog
import org.oppia.app.model.EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
import org.oppia.app.model.EventLog.Context.ActivityContextCase.CONCEPT_CARD_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.QUESTION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.REVISION_CARD_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.STORY_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.TOPIC_CONTEXT

const val TIMESTAMP_KEY = "timestamp"
const val TOPIC_ID_KEY = "topicId"
const val STORY_ID_KEY = "storyId"
const val SKILL_ID_KEY = "skillId"
const val SKILL_LIST_ID_KEY = "skillListId"
const val SUB_TOPIC_ID_KEY = "subTopicId"
const val QUESTION_ID_KEY = "questionId"
const val EXPLORATION_ID_KEY = "explorationId"
const val PRIORITY_KEY = "priority"

/**
 * Utility for creating bundles from [EventLog] objects.
 * Note that this utility may later upload them to remote services.
 */
class EventBundleCreator {
  private var bundle = Bundle()

  fun createEventBundle(eventLog: EventLog): Bundle {
    bundle =
      when (eventLog.context.activityContextCase) {
        EXPLORATION_CONTEXT -> createExplorationContextBundle(eventLog)
        QUESTION_CONTEXT -> createQuestionContextBundle(eventLog)
        STORY_CONTEXT -> createStoryContextBundle(eventLog)
        TOPIC_CONTEXT -> createTopicContextBundle(eventLog)
        CONCEPT_CARD_CONTEXT -> createConceptCardContextBundle(eventLog)
        REVISION_CARD_CONTEXT -> createRevisionCardContextBundle(eventLog)
        ACTIVITYCONTEXT_NOT_SET -> createNoContextBundle(eventLog)
      }
    return bundle
  }

  /** Returns a bundle from event having exploration context. */
  private fun createExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.explorationContext.topicId)
    bundle.putString(STORY_ID_KEY, eventLog.context.explorationContext.storyId)
    bundle.putString(EXPLORATION_ID_KEY, eventLog.context.explorationContext.explorationId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having question context. */
  private fun createQuestionContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(QUESTION_ID_KEY, eventLog.context.questionContext.questionId)
    bundle.putStringArray(
      SKILL_LIST_ID_KEY,
      eventLog.context.questionContext.skillIdList.toTypedArray()
    )
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having question context. */
  private fun createTopicContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.topicContext.topicId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having question context. */
  private fun createStoryContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.storyContext.topicId)
    bundle.putString(STORY_ID_KEY, eventLog.context.storyContext.storyId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having concept card context. */
  private fun createConceptCardContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(SKILL_ID_KEY, eventLog.context.conceptCardContext.skillId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having revision card context. */
  private fun createRevisionCardContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.revisionCardContext.topicId)
    bundle.putString(SUB_TOPIC_ID_KEY, eventLog.context.revisionCardContext.subTopicId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having no context. */
  private fun createNoContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }
}

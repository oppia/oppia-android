package org.oppia.android.util.logging

import android.os.Bundle
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_CONCEPT_CARD
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_EXPLORATION_ACTIVITY
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_HOME
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_INFO_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_LESSONS_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_PRACTICE_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_PROFILE_CHOOSER
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_QUESTION_PLAYER
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_REVISION_CARD
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_REVISION_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_STORY_ACTIVITY

const val TIMESTAMP_KEY = "timestamp"
const val TOPIC_ID_KEY = "topicId"
const val STORY_ID_KEY = "storyId"
const val SKILL_ID_KEY = "skillId"
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
        OPEN_EXPLORATION_ACTIVITY -> createOpenExplorationActivityContextBundle(eventLog)
        OPEN_QUESTION_PLAYER -> createOpenQuestionPlayerContextBundle(eventLog)
        OPEN_STORY_ACTIVITY -> createOpenStoryActivityContextBundle(eventLog)
        OPEN_INFO_TAB -> createOpenInfoTabContextBundle(eventLog)
        OPEN_LESSONS_TAB -> createOpenLessonsTabContextBundle(eventLog)
        OPEN_PRACTICE_TAB -> createOpenPracticeTabContextBundle(eventLog)
        OPEN_REVISION_TAB -> createOpenRevisionTabContextBundle(eventLog)
        OPEN_CONCEPT_CARD -> createOpenConceptCardContextBundle(eventLog)
        OPEN_REVISION_CARD -> createOpenRevisionCardContextBundle(eventLog)
        OPEN_HOME, OPEN_PROFILE_CHOOSER, ACTIVITYCONTEXT_NOT_SET -> createNoContextBundle(eventLog)
        // TODO(#4064): Create bundle creator functions for new events and replace this with them.
        else -> createNoContextBundle(eventLog)
      }
    return bundle
  }

  /** Returns a bundle from event having open_exploration_activity context. */
  private fun createOpenExplorationActivityContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.openExplorationActivity.topicId)
    bundle.putString(STORY_ID_KEY, eventLog.context.openExplorationActivity.storyId)
    bundle.putString(EXPLORATION_ID_KEY, eventLog.context.openExplorationActivity.explorationId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having open_question_player context. */
  private fun createOpenQuestionPlayerContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    val skillIdList = eventLog.context.openQuestionPlayer.skillIdList
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(QUESTION_ID_KEY, eventLog.context.openQuestionPlayer.questionId)
    bundle.putString(SKILL_ID_KEY, skillIdList.joinToString())
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having open_info_tab context. */
  private fun createOpenInfoTabContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.openInfoTab.topicId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having open_lessons_tab context. */
  private fun createOpenLessonsTabContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.openLessonsTab.topicId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having open_practice_tab context. */
  private fun createOpenPracticeTabContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.openPracticeTab.topicId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having open_revision_tab context. */
  private fun createOpenRevisionTabContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.openRevisionTab.topicId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having open_story_activity context. */
  private fun createOpenStoryActivityContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.openStoryActivity.topicId)
    bundle.putString(STORY_ID_KEY, eventLog.context.openStoryActivity.storyId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having open_concept_card context. */
  private fun createOpenConceptCardContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(SKILL_ID_KEY, eventLog.context.openConceptCard.skillId)
    bundle.putString(PRIORITY_KEY, eventLog.priority.toString())
    return bundle
  }

  /** Returns a bundle from event having open_revision_card context. */
  private fun createOpenRevisionCardContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(TOPIC_ID_KEY, eventLog.context.openRevisionCard.topicId)
    bundle.putInt(SUB_TOPIC_ID_KEY, eventLog.context.openRevisionCard.subTopicId)
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

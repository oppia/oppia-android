package org.oppia.android.util.logging

import android.os.Bundle
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_HINT_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_SOLUTION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_BACKGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.CONCEPT_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.DELETE_PROFILE_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.END_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.EXIT_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.FINISH_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.HINT_OFFERED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.PLAY_VOICE_OVER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.QUESTION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RESUME_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.REVISION_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SOLUTION_OFFERED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_OVER_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.STORY_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SUBMIT_ANSWER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.TOPIC_CONTEXT

const val TIMESTAMP_KEY = "timestamp"
const val TOPIC_ID_KEY = "topicId"
const val STORY_ID_KEY = "storyId"
const val SKILL_ID_KEY = "skillId"
const val SUB_TOPIC_ID_KEY = "subTopicId"
const val QUESTION_ID_KEY = "questionId"
const val EXPLORATION_ID_KEY = "explorationId"
const val PRIORITY_KEY = "priority"
const val LEARNER_ID_KEY_LEARNER_ANALYTICS = "profile_id"
const val DEVICE_ID_KEY_LEARNER_ANALYTICS = "device_id"
const val SESSION_ID_KEY_LEARNER_ANALYTICS = "session_id"
const val EXPLORATION_ID_KEY_LEARNER_ANALYTICS = "exploration_id"
const val EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS = "exploration_version"
const val STATE_NAME_KEY_LEARNER_ANALYTICS = "state_name"
const val SKILL_ID_KEY_LEARNER_ANALYTICS = "card_skill_id"
const val HINT_INDEX_KEY_LEARNER_ANALYTICS = "hint_index"
const val ANSWER_LABEL_KEY_LEARNER_ANALYTICS = "is_answer_correct"
const val CONTENT_ID_KEY_LEARNER_ANALYTICS = "content_id"
const val START_CARD_TIMESTAMP_KEY = "card_start_timestamp"
const val END_CARD_TIMESTAMP_KEY = "end_card_timestamp"
const val HINT_OFFERED_TIMESTAMP_KEY = "hint_offered_timestamp"
const val ACCESS_HINT_TIMESTAMP_KEY = "hint_accessed_timestamp"
const val SOLUTION_OFFERED_TIMESTAMP_KEY = "solution_offered_timestamp"
const val ACCESS_SOLUTION_TIMESTAMP_KEY = "solution_accessed_timestamp"
const val SUBMIT_ANSWER_TIMESTAMP_KEY = "submit_answer_timestamp"
const val PLAY_VOICE_OVER_TIMESTAMP_KEY = "play_voiceover_timestamp"
const val APP_IN_BACKGROUND_TIMESTAMP_KEY = "background_app_inactive_timestamp"
const val APP_IN_FOREGROUND_TIMESTAMP_KEY = "foreground_app_inactive_timestamp"
const val EXIT_EXPLORATION_TIMESTAMP_KEY = "exit_exploration_timestamp"
const val FINISH_EXPLORATION_TIMESTAMP_KEY = "finish_exploration_timestamp"
const val RESUME_EXPLORATION_TIMESTAMP_KEY = "resume_lesson_timestamp"
const val START_OVER_EXPLORATION_TIMESTAMP_KEY = "start_over_lesson_timestamp"
const val DELETE_PROFILE_TIMESTAMP_KEY = "start_delete_profile_timestamp"

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
        START_CARD_CONTEXT -> createStartCardContextBundle(eventLog)
        END_CARD_CONTEXT -> createEndCardContextBundle(eventLog)
        ACTIVITYCONTEXT_NOT_SET -> createNoContextBundle(eventLog)
        HINT_OFFERED_CONTEXT -> createHintOfferedContextBundle(eventLog)
        ACCESS_HINT_CONTEXT -> createAccessHintContextBundle(eventLog)
        SOLUTION_OFFERED_CONTEXT -> createSolutionOfferedContextBundle(eventLog)
        ACCESS_SOLUTION_CONTEXT -> createAccessSolutionContextBundle(eventLog)
        SUBMIT_ANSWER_CONTEXT -> createSubmitAnswerContextBundle(eventLog)
        PLAY_VOICE_OVER_CONTEXT -> createPlayVoiceOverContextBundle(eventLog)
        APP_IN_BACKGROUND_CONTEXT -> createAppInBackgroundContextBundle(eventLog)
        APP_IN_FOREGROUND_CONTEXT -> createAppInForegroundContextBundle(eventLog)
        EXIT_EXPLORATION_CONTEXT -> createExitExplorationContextBundle(eventLog)
        FINISH_EXPLORATION_CONTEXT -> createFinishExplorationContextBundle(eventLog)
        RESUME_EXPLORATION_CONTEXT -> createResumeExplorationContextBundle(eventLog)
        START_OVER_EXPLORATION_CONTEXT -> createStartOverExplorationContextBundle(eventLog)
        DELETE_PROFILE_CONTEXT -> createDeleteProfileContextBundle(eventLog)
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

  /** Returns a bundle from event having start card context. */
  private fun createStartCardContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(START_CARD_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationData.stateName
    )
    bundle.putString(SKILL_ID_KEY_LEARNER_ANALYTICS, eventLog.context.startCardContext.skillId)
    return bundle
  }

  /** Returns a bundle from event having end card context. */
  private fun createEndCardContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(END_CARD_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.explorationData.stateName
    )
    bundle.putString(SKILL_ID_KEY_LEARNER_ANALYTICS, eventLog.context.endCardContext.skillId)
    return bundle
  }

  /** Returns a bundle from event having hint offered context. */
  private fun createHintOfferedContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(HINT_OFFERED_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.explorationData.stateName
    )
    bundle.putString(
      HINT_INDEX_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.hintIndex
    )
    return bundle
  }

  /** Returns a bundle from event having access hint context. */
  private fun createAccessHintContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(ACCESS_HINT_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationData.stateName
    )
    bundle.putString(HINT_INDEX_KEY_LEARNER_ANALYTICS, eventLog.context.accessHintContext.hintIndex)
    return bundle
  }

  /** Returns a bundle from event having solution offered context. */
  private fun createSolutionOfferedContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(SOLUTION_OFFERED_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.solutionOfferedContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.solutionOfferedContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.solutionOfferedContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.solutionOfferedContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.solutionOfferedContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.solutionOfferedContext.explorationData.stateName
    )
    return bundle
  }

  /** Returns a bundle from event having access solution context. */
  private fun createAccessSolutionContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(ACCESS_SOLUTION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessSolutionContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessSolutionContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessSolutionContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessSolutionContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessSolutionContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessSolutionContext.explorationData.stateName
    )
    return bundle
  }

  /** Returns a bundle from event having submit answer context. */
  private fun createSubmitAnswerContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(SUBMIT_ANSWER_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationData.stateName
    )
    bundle.putString(
      ANSWER_LABEL_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.isAnswerCorrect.toString()
    )
    return bundle
  }

  /** Returns a bundle from event having play voiceover context. */
  private fun createPlayVoiceOverContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(PLAY_VOICE_OVER_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.explorationData.stateName
    )
    bundle.putString(
      CONTENT_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.contentId.toString()
    )
    return bundle
  }

  /** Returns a bundle from event having app in background context. */
  private fun createAppInBackgroundContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(APP_IN_BACKGROUND_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.appInBackgroundContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.appInBackgroundContext.genericData.deviceId
    )
    return bundle
  }

  /** Returns a bundle from event having app in foreground context. */
  private fun createAppInForegroundContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(APP_IN_FOREGROUND_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.appInForegroundContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.appInForegroundContext.genericData.deviceId
    )
    return bundle
  }

  /** Returns a bundle from event having exit exploration context. */
  private fun createExitExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(EXIT_EXPLORATION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.exitExplorationContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.exitExplorationContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.exitExplorationContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.exitExplorationContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.exitExplorationContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.exitExplorationContext.explorationData.stateName
    )
    return bundle
  }

  /** Returns a bundle from event having finish exploration context. */
  private fun createFinishExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(FINISH_EXPLORATION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.finishExplorationContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.finishExplorationContext.genericData.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.finishExplorationContext.explorationData.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.finishExplorationContext.explorationData.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.finishExplorationContext.explorationData.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.finishExplorationContext.explorationData.stateName
    )
    return bundle
  }

  /** Returns a bundle from event having resume exploration context. */
  private fun createResumeExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(RESUME_EXPLORATION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.resumeExplorationContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.resumeExplorationContext.genericData.deviceId
    )
    return bundle
  }

  /** Returns a bundle from event having start over exploration context. */
  private fun createStartOverExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(START_OVER_EXPLORATION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startOverExplorationContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startOverExplorationContext.genericData.deviceId
    )
    return bundle
  }

  /** Returns a bundle from event having delete profile context. */
  private fun createDeleteProfileContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(DELETE_PROFILE_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.deleteProfileContext.genericData.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.deleteProfileContext.genericData.deviceId
    )
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

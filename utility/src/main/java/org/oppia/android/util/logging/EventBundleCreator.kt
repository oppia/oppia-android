package org.oppia.android.util.logging

import android.os.Bundle
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_HINT_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_SOLUTION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_BACKGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.DELETE_PROFILE_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.END_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.EXIT_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.FINISH_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.HINT_OFFERED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.PLAY_VOICE_OVER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RESUME_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SOLUTION_OFFERED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_OVER_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SUBMIT_ANSWER_CONTEXT

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

  /** Returns a bundle from event having start card context. */
  private fun createStartCardContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(START_CARD_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationDetails.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationDetails.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationDetails.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationDetails.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationDetails.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.startCardContext.explorationDetails.stateName
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
      eventLog.context.endCardContext.explorationDetails.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.explorationDetails.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.explorationDetails.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.explorationDetails.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.endCardContext.explorationDetails.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS, eventLog.context.endCardContext.explorationDetails.stateName
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
      eventLog.context.hintOfferedContext.explorationDetails.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.explorationDetails.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.explorationDetails.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.explorationDetails.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.explorationDetails.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.hintOfferedContext.explorationDetails.stateName
    )
    bundle.putInt(HINT_INDEX_KEY_LEARNER_ANALYTICS, eventLog.context.hintOfferedContext.hintIndex)
    return bundle
  }

  /** Returns a bundle from event having access hint context. */
  private fun createAccessHintContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(ACCESS_HINT_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationDetails.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationDetails.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationDetails.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationDetails.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationDetails.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessHintContext.explorationDetails.stateName
    )
    bundle.putInt(HINT_INDEX_KEY_LEARNER_ANALYTICS, eventLog.context.accessHintContext.hintIndex)
    return bundle
  }

  /** Returns a bundle from event having solution offered context. */
  private fun createSolutionOfferedContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(SOLUTION_OFFERED_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.solutionOfferedContext.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.solutionOfferedContext.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS, eventLog.context.solutionOfferedContext.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS, eventLog.context.solutionOfferedContext.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.solutionOfferedContext.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS, eventLog.context.solutionOfferedContext.stateName
    )
    return bundle
  }

  /** Returns a bundle from event having access solution context. */
  private fun createAccessSolutionContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(ACCESS_SOLUTION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessSolutionContext.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessSolutionContext.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS, eventLog.context.accessSolutionContext.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS, eventLog.context.accessSolutionContext.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.accessSolutionContext.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS, eventLog.context.accessSolutionContext.stateName
    )
    return bundle
  }

  /** Returns a bundle from event having submit answer context. */
  private fun createSubmitAnswerContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(SUBMIT_ANSWER_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationDetails.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationDetails.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationDetails.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationDetails.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationDetails.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.submitAnswerContext.explorationDetails.stateName
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
      eventLog.context.playVoiceOverContext.explorationDetails.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.explorationDetails.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.explorationDetails.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.explorationDetails.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.explorationDetails.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS,
      eventLog.context.playVoiceOverContext.explorationDetails.stateName
    )
    bundle.putString(
      CONTENT_ID_KEY_LEARNER_ANALYTICS, eventLog.context.playVoiceOverContext.contentId
    )
    return bundle
  }

  /** Returns a bundle from event having app in background context. */
  private fun createAppInBackgroundContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(APP_IN_BACKGROUND_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS, eventLog.context.appInBackgroundContext.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS, eventLog.context.appInBackgroundContext.deviceId
    )
    return bundle
  }

  /** Returns a bundle from event having app in foreground context. */
  private fun createAppInForegroundContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(APP_IN_FOREGROUND_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS, eventLog.context.appInForegroundContext.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS, eventLog.context.appInForegroundContext.deviceId
    )
    return bundle
  }

  /** Returns a bundle from event having exit exploration context. */
  private fun createExitExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(EXIT_EXPLORATION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.exitExplorationContext.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.exitExplorationContext.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS, eventLog.context.exitExplorationContext.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS, eventLog.context.exitExplorationContext.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.exitExplorationContext.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS, eventLog.context.exitExplorationContext.stateName
    )
    return bundle
  }

  /** Returns a bundle from event having finish exploration context. */
  private fun createFinishExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(FINISH_EXPLORATION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.finishExplorationContext.learnerDetails.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS,
      eventLog.context.finishExplorationContext.learnerDetails.deviceId
    )
    bundle.putString(
      SESSION_ID_KEY_LEARNER_ANALYTICS, eventLog.context.finishExplorationContext.sessionId
    )
    bundle.putString(
      EXPLORATION_ID_KEY_LEARNER_ANALYTICS, eventLog.context.finishExplorationContext.explorationId
    )
    bundle.putString(
      EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS,
      eventLog.context.finishExplorationContext.explorationVersion
    )
    bundle.putString(
      STATE_NAME_KEY_LEARNER_ANALYTICS, eventLog.context.finishExplorationContext.stateName
    )
    return bundle
  }

  /** Returns a bundle from event having resume exploration context. */
  private fun createResumeExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(RESUME_EXPLORATION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS, eventLog.context.resumeExplorationContext.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS, eventLog.context.resumeExplorationContext.deviceId
    )
    return bundle
  }

  /** Returns a bundle from event having start over exploration context. */
  private fun createStartOverExplorationContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(START_OVER_EXPLORATION_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS, eventLog.context.startOverExplorationContext.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS, eventLog.context.startOverExplorationContext.deviceId
    )
    return bundle
  }

  /** Returns a bundle from event having delete profile context. */
  private fun createDeleteProfileContextBundle(eventLog: EventLog): Bundle {
    val bundle = Bundle()
    bundle.putLong(DELETE_PROFILE_TIMESTAMP_KEY, eventLog.timestamp)
    bundle.putString(
      LEARNER_ID_KEY_LEARNER_ANALYTICS, eventLog.context.deleteProfileContext.learnerId
    )
    bundle.putString(
      DEVICE_ID_KEY_LEARNER_ANALYTICS, eventLog.context.deleteProfileContext.deviceId
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

package org.oppia.android.util.logging

import org.oppia.android.app.model.EventLog.Context.ActivityContextCase
import javax.inject.Inject

// TODO(#4419): Remove this implementation and the extra piping for event name mapping.
/**
 * Implementation of [EventTypeToHumanReadableNameConverter] which provides legacy event names for
 * interoperability with previously logged data in the Kenya user study.
 *
 * The event names provided by this implementation are expected to never change as these names are
 * the primary ways each corresponding event is identified in logged event queries.
 */
class KenyaAlphaEventTypeToHumanReadableNameConverterImpl @Inject constructor() :
  EventTypeToHumanReadableNameConverter {
  override fun convertToHumanReadableName(eventType: ActivityContextCase): String {
    return when (eventType) {
      ActivityContextCase.OPEN_EXPLORATION_ACTIVITY -> "open_exploration_activity"
      ActivityContextCase.OPEN_INFO_TAB -> "open_info_tab"
      ActivityContextCase.OPEN_LESSONS_TAB -> "open_lessons_tab"
      ActivityContextCase.OPEN_PRACTICE_TAB -> "open_practice_tab"
      ActivityContextCase.OPEN_REVISION_TAB -> "open_revision_tab"
      ActivityContextCase.OPEN_QUESTION_PLAYER -> "open_question_player"
      ActivityContextCase.OPEN_STORY_ACTIVITY -> "open_story_activity"
      ActivityContextCase.OPEN_CONCEPT_CARD -> "open_concept_card"
      ActivityContextCase.OPEN_REVISION_CARD -> "open_revision_card"
      ActivityContextCase.START_CARD_CONTEXT -> "start_card_context"
      ActivityContextCase.END_CARD_CONTEXT -> "end_card_context"
      ActivityContextCase.HINT_OFFERED_CONTEXT -> "hint_offered_context"
      ActivityContextCase.ACCESS_HINT_CONTEXT -> "access_hint_context"
      ActivityContextCase.SOLUTION_OFFERED_CONTEXT -> "solution_offered_context"
      ActivityContextCase.ACCESS_SOLUTION_CONTEXT -> "access_solution_context"
      ActivityContextCase.SUBMIT_ANSWER_CONTEXT -> "submit_answer_context"
      ActivityContextCase.PLAY_VOICE_OVER_CONTEXT -> "play_voice_over_context"
      ActivityContextCase.APP_IN_BACKGROUND_CONTEXT -> "app_in_background_context"
      ActivityContextCase.APP_IN_FOREGROUND_CONTEXT -> "app_in_foreground_context"
      ActivityContextCase.EXIT_EXPLORATION_CONTEXT -> "exit_exploration_context"
      ActivityContextCase.FINISH_EXPLORATION_CONTEXT -> "finish_exploration_context"
      ActivityContextCase.RESUME_EXPLORATION_CONTEXT -> "resume_exploration_context"
      ActivityContextCase.START_OVER_EXPLORATION_CONTEXT -> "start_over_exploration_context"
      ActivityContextCase.DELETE_PROFILE_CONTEXT -> "delete_profile_context"
      ActivityContextCase.OPEN_HOME -> "open_home"
      ActivityContextCase.OPEN_PROFILE_CHOOSER -> "open_profile_chooser"
      ActivityContextCase.REACH_INVESTED_ENGAGEMENT -> "reached_invested_engagement"
      ActivityContextCase.INSTALL_ID_FOR_FAILED_ANALYTICS_LOG -> "failed_analytics_log"
      ActivityContextCase.ACTIVITYCONTEXT_NOT_SET -> "unknown_activity_context"
    }
  }
}

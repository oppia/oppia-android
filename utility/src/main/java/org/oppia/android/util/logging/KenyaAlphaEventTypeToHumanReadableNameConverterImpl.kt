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
      ActivityContextCase.CLOSE_REVISION_CARD -> "close_revision_card"
      ActivityContextCase.START_CARD_CONTEXT -> "start_card_context"
      ActivityContextCase.END_CARD_CONTEXT -> "end_card_context"
      ActivityContextCase.HINT_UNLOCKED_CONTEXT -> "hint_offered_context"
      ActivityContextCase.REVEAL_HINT_CONTEXT -> "reveal_hint_context"
      ActivityContextCase.VIEW_EXISTING_HINT_CONTEXT -> "view_existing_hint_context"
      ActivityContextCase.SOLUTION_UNLOCKED_CONTEXT -> "solution_offered_context"
      ActivityContextCase.REVEAL_SOLUTION_CONTEXT -> "reveal_solution_context"
      ActivityContextCase.VIEW_EXISTING_SOLUTION_CONTEXT -> "view_existing_solution_context"
      ActivityContextCase.SUBMIT_ANSWER_CONTEXT -> "submit_answer_context"
      ActivityContextCase.PLAY_VOICE_OVER_CONTEXT -> "play_voice_over_context"
      ActivityContextCase.PAUSE_VOICE_OVER_CONTEXT -> "pause_voice_over_context"
      ActivityContextCase.APP_IN_BACKGROUND_CONTEXT -> "app_in_background_context"
      ActivityContextCase.APP_IN_FOREGROUND_CONTEXT -> "app_in_foreground_context"
      ActivityContextCase.START_EXPLORATION_CONTEXT -> "start_exploration_context"
      ActivityContextCase.EXIT_EXPLORATION_CONTEXT -> "exit_exploration_context"
      ActivityContextCase.FINISH_EXPLORATION_CONTEXT -> "finish_exploration_context"
      ActivityContextCase.PROGRESS_SAVING_SUCCESS_CONTEXT -> "progress_saving_success_context"
      ActivityContextCase.PROGRESS_SAVING_FAILURE_CONTEXT -> "progress_saving_failure_context"
      ActivityContextCase.LESSON_SAVED_ADVERTENTLY_CONTEXT -> "lesson_saved_advertently_context"
      ActivityContextCase.RESUME_LESSON_SUBMIT_CORRECT_ANSWER_CONTEXT ->
        "resume_lesson_submit_correct_answer_context"
      ActivityContextCase.RESUME_LESSON_SUBMIT_INCORRECT_ANSWER_CONTEXT ->
        "resume_lesson_submit_incorrect_answer_context"
      ActivityContextCase.RESUME_EXPLORATION_CONTEXT -> "resume_exploration_context"
      ActivityContextCase.START_OVER_EXPLORATION_CONTEXT -> "start_over_exploration_context"
      ActivityContextCase.DELETE_PROFILE_CONTEXT -> "delete_profile_context"
      ActivityContextCase.OPEN_HOME -> "open_home"
      ActivityContextCase.OPEN_PROFILE_CHOOSER -> "open_profile_chooser"
      ActivityContextCase.REACH_INVESTED_ENGAGEMENT -> "reached_invested_engagement"
      ActivityContextCase.SWITCH_IN_LESSON_LANGUAGE -> "switch_in_lesson_language"
      ActivityContextCase.SHOW_SURVEY_POPUP -> "show_survey_popup"
      ActivityContextCase.BEGIN_SURVEY -> "begin_survey"
      ActivityContextCase.ABANDON_SURVEY -> "abandon_survey"
      ActivityContextCase.MANDATORY_RESPONSE -> "mandatory_response"
      ActivityContextCase.OPTIONAL_RESPONSE -> "optional_response"
      ActivityContextCase.FEATURE_FLAG_LIST_CONTEXT -> "feature_flag_list"
      ActivityContextCase.INSTALL_ID_FOR_FAILED_ANALYTICS_LOG -> "failed_analytics_log"
      ActivityContextCase.ACTIVITYCONTEXT_NOT_SET -> "unknown_activity_context"
      ActivityContextCase.COMPLETE_APP_ONBOARDING -> "complete_app_onboarding"
      ActivityContextCase.CONSOLE_LOG -> "console_log"
      ActivityContextCase.RETROFIT_CALL_CONTEXT -> "retrofit_call_context"
      ActivityContextCase.RETROFIT_CALL_FAILED_CONTEXT -> "retrofit_call_failed_context"
      ActivityContextCase.APP_IN_FOREGROUND_TIME -> "app_in_foreground_time"
      ActivityContextCase.START_PROFILE_ONBOARDING_EVENT -> "start_profile_onboarding_event"
      ActivityContextCase.END_PROFILE_ONBOARDING_EVENT -> "end_profile_onboarding_event"
    }
  }
}

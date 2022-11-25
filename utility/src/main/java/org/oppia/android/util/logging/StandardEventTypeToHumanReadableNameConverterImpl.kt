package org.oppia.android.util.logging

import org.oppia.android.app.model.EventLog.Context.ActivityContextCase
import javax.inject.Inject

/**
 * Implementation of [EventTypeToHumanReadableNameConverter] which provides well-named
 * representations for each event type.
 *
 * The provided names may be changed over time as the broad standard for event names evolves with
 * new and changed events. The general convention for event names is a second person singular
 * conjugated action verb followed by the action, context, or both, that the user conducted that led
 * to that event. Furthermore, efforts are taken to reduce referencing Android-specific terminology
 * (e.g. 'screen' is used in place of 'activity').
 *
 * Event names cannot exceed 40 characters, so care should be taken to ensure only the most
 * important information is conveyed via the name.
 *
 * Finally, the provided event names are only meant to help simplify analyzing logged events. Since
 * they can change, aggregation or dimensional slicing of events should occur by keying on the
 * event's integral type rather than its name (as this type will remain fixed for the lifetime of a
 * given event, unlike its name returned by this implementation).
 */
class StandardEventTypeToHumanReadableNameConverterImpl @Inject constructor() :
  EventTypeToHumanReadableNameConverter {
  override fun convertToHumanReadableName(eventType: ActivityContextCase): String {
    return when (eventType) {
      ActivityContextCase.OPEN_EXPLORATION_ACTIVITY -> "open_exploration_player_screen"
      ActivityContextCase.OPEN_INFO_TAB -> "select_topic_info_tab"
      ActivityContextCase.OPEN_LESSONS_TAB -> "select_topic_lessons_tab"
      ActivityContextCase.OPEN_PRACTICE_TAB -> "select_topic_practice_tab"
      ActivityContextCase.OPEN_REVISION_TAB -> "select_topic_revision_tab"
      ActivityContextCase.OPEN_QUESTION_PLAYER -> "open_question_player_screen"
      ActivityContextCase.OPEN_STORY_ACTIVITY -> "open_story_chapter_list_screen"
      ActivityContextCase.OPEN_CONCEPT_CARD -> "open_concept_card"
      ActivityContextCase.OPEN_REVISION_CARD -> "open_revision_card"
      ActivityContextCase.START_CARD_CONTEXT -> "start_exploration_card"
      ActivityContextCase.END_CARD_CONTEXT -> "end_exploration_card"
      ActivityContextCase.HINT_OFFERED_CONTEXT -> "receive_hint_offer"
      ActivityContextCase.ACCESS_HINT_CONTEXT -> "reveal_hint"
      ActivityContextCase.SOLUTION_OFFERED_CONTEXT -> "receive_solution_offer"
      ActivityContextCase.ACCESS_SOLUTION_CONTEXT -> "reveal_solution"
      ActivityContextCase.SUBMIT_ANSWER_CONTEXT -> "submit_answer"
      ActivityContextCase.PLAY_VOICE_OVER_CONTEXT -> "click_play_voiceover_button"
      ActivityContextCase.APP_IN_BACKGROUND_CONTEXT -> "send_app_to_background"
      ActivityContextCase.APP_IN_FOREGROUND_CONTEXT -> "bring_app_to_foreground"
      ActivityContextCase.EXIT_EXPLORATION_CONTEXT -> "leave_exploration"
      ActivityContextCase.FINISH_EXPLORATION_CONTEXT -> "complete_exploration"
      ActivityContextCase.RESUME_EXPLORATION_CONTEXT -> "resume_in_progress_exploration"
      ActivityContextCase.START_OVER_EXPLORATION_CONTEXT -> "restart_in_progress_exploration"
      ActivityContextCase.DELETE_PROFILE_CONTEXT -> "delete_profile"
      ActivityContextCase.OPEN_HOME -> "open_home_screen"
      ActivityContextCase.OPEN_PROFILE_CHOOSER -> "open_profile_chooser_screen"
      ActivityContextCase.REACH_INVESTED_ENGAGEMENT -> "reach_invested_engagement"
      ActivityContextCase.INSTALL_ID_FOR_FAILED_ANALYTICS_LOG,
      ActivityContextCase.ACTIVITYCONTEXT_NOT_SET -> "ERROR_internal_logging_failure"
    }
  }
}

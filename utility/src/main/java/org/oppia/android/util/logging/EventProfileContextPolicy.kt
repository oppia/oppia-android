package org.oppia.android.util.logging

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase

/**
 * Determines which events may include profile metadata in product analytics.
 *
 * Only explicitly listed contexts containing content or navigation metadata are allowed.
 * Answers, surveys, diagnostics, study identifiers, and unreviewed contexts are excluded.
 */
object EventProfileContextPolicy {
  /** Returns whether [context] may be associated with the caller's explicitly supplied profile. */
  fun canIncludeProfileContext(context: EventLog.Context): Boolean =
    when (context.activityContextCase) {
      ActivityContextCase.OPEN_INFO_TAB,
      ActivityContextCase.OPEN_HOME,
      ActivityContextCase.OPEN_LESSONS_TAB,
      ActivityContextCase.OPEN_PRACTICE_TAB,
      ActivityContextCase.OPEN_REVISION_TAB,
      ActivityContextCase.OPEN_QUESTION_PLAYER,
      ActivityContextCase.OPEN_STORY_ACTIVITY,
      ActivityContextCase.OPEN_CONCEPT_CARD,
      ActivityContextCase.OPEN_REVISION_CARD,
      ActivityContextCase.CLOSE_REVISION_CARD -> true
      else -> false
    }
}

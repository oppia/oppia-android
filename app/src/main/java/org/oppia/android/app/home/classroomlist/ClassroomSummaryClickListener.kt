package org.oppia.android.app.home.classroomlist

import org.oppia.android.app.model.ClassroomSummary

/** Listener interface for when classroom summaries are clicked in the UI. */
interface ClassroomSummaryClickListener {
  /** Called when a classroom card is clicked by the user. */
  fun onClassroomSummaryClicked(classroomSummary: ClassroomSummary)
}

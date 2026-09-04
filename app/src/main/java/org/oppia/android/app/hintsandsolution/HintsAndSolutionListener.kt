package org.oppia.android.app.hintsandsolution

/** Allows parent activity to listen to events from [HintsAndSolutionDialogFragment]. */
interface HintsAndSolutionListener {
  /** Called when the hints and solution dialog should be dismissed. */
  fun dismiss()

  /** Called when the hints and solution dialog is opened and hint timers should be paused. */
  fun pauseHints()

  /** Called when the hints and solution dialog is closed and hint timers should be resumed. */
  fun resumeHints()
}

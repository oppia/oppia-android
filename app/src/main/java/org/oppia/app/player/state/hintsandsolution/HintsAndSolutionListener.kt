package org.oppia.app.player.state.hintsandsolution

/** Allows parent activity to dismiss the [HintsAndSolutionFragmentt] */
interface HintsAndSolutionListener {
  /** Called when the hints and solution dialog should be dismissed. */
  fun dismiss()
}

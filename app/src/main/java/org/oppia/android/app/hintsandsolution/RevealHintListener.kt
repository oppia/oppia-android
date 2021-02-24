package org.oppia.android.app.hintsandsolution

/** Interface to check the preference regarding alert for [HintsAndSolutionDialogFragment]. */
interface RevealHintListener {
  /**
   * If saveUserChoice is true, show solution and save preference do not show dialog again.
   * If saveUserChoice is false, show solution and do not save preference and show this dialog next time too.
   */
  fun revealHint(saveUserChoice: Boolean, hintIndex: Int)
}

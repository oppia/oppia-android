package org.oppia.android.app.hintsandsolution

/** Callback listener for when the user wishes to view a hint. */
interface ViewHintListener {
  /**
   * Called when the user indicates they want to view the hint corresponding to the specified
   * index.
   */
  fun viewHint(hintIndex: Int)
}

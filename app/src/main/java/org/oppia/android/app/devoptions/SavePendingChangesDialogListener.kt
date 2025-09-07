package org.oppia.android.app.devoptions

/** Interface to be implemented by classes that need to handle pending changes. */
interface SavePendingChangesDialogListener {

  /** Saves the pending changes. */
  fun savePendingChanges()

  /** Discards the pending changes and exits the screen. */
  fun discardPendingChanges()
}

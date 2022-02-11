package org.oppia.android.app.administratorcontrols

/** Listener for when the user clicks on the [ProfileEditDeletionDialogFragment]. */
interface ProfileDeletionDialogListener {
  /** Checks [ProfileEditDeletionDialogFragment] for the configuration changes the clicked state of the dialog. */
  fun loadBooleanProfileDeletionDialog(isProfileDeletionDialogVisible: Boolean)
}

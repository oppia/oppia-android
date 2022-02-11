package org.oppia.android.app.settings.profile

/** Listener for when the activity should inflate [ProfileEditDeletionDialogFragment]. */
interface LoadProfileEditDeletionDialogListener {
  /** Inflates [ProfileEditDeletionDialogFragment] for the configuration changes and saves the state of the dialog. */
  fun loadProfileEditDeletionDialog(internalProfileId: Int)
}

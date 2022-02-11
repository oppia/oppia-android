package org.oppia.android.app.settings.profile

/** Listener for when the activity should inflate [ProfileEditDeletionDialogFragment]. */
interface LoadProfileDeletionDialogListener {
  /** Inflates [ProfileEditDeletionDialogFragment] for the configuration changes and saves the state of the dialog. */
  fun loadProfileDeletionDialog(internalProfileId: Int)
}

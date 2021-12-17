package org.oppia.android.app.settings.profile

/** Interface to handle option selection in [ProfileEditDeletionDialogFragment]. */
interface ProfileEditDialogInterface {

  /** Handles profile deletion in [ProfileEditFragment]. */
  fun deleteProfileByInternalProfileId(internalProfileId: Int)
}

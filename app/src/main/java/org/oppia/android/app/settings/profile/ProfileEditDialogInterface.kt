package org.oppia.android.app.settings.profile

/** Interface to handle option selection in [ProfileEditDeletionDialogFragment]. */
interface ProfileEditDialogInterface {

  /** An function defination for profile deletion in [ProfileEditActivity]. */
  fun deleteProfileByInternalProfileId(internalProfileId: Int)
}

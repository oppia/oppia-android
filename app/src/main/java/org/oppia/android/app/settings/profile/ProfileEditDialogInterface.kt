package org.oppia.android.app.settings.profile

import org.oppia.android.app.model.ProfileId

/** Interface to handle option selection in [ProfileEditDeletionDialogFragment]. */
interface ProfileEditDialogInterface {

  /** Handles profile deletion in [ProfileEditFragment]. */
  fun deleteProfileByInternalProfileId(profileId: ProfileId)
}

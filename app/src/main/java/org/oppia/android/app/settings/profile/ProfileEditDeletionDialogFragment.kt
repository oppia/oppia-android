package org.oppia.android.app.settings.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId

/** [DialogFragment] that gives option to delete profile. */
class ProfileEditDeletionDialogFragment : InjectableDialogFragment() {

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    /** Argument key for pop up of Profile Deletion Dialog in [ProfileEditActivity]. */
    const val PROFILE_DELETION_DIALOG_INTERNAL_PROFILE_ID_EXTRA_KEY =
      "ProfileEditDeletionDialogFragment.profile_deletion_dialog_internal_profile_id"

    /** Creates new instance of the fragment [ProfileEditFragment]. */
    fun newInstance(internalProfileId: Int): ProfileEditDeletionDialogFragment {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      val profileEditDeletionDialog = ProfileEditDeletionDialogFragment()
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
      profileEditDeletionDialog.arguments = args
      return profileEditDeletionDialog
    }
  }

  /** A dialog interface for creating dialogs of [ProfileEditDialogFragment]. */
  lateinit var profileEditDialogInterface: ProfileEditDialogInterface

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args =
      checkNotNull(arguments) {
        "Expected arguments to be pass to ProfileEditDeletionDialogFragment"
      }

    val internalProfileId = args.extractCurrentUserProfileId().loggedInInternalProfileId
    profileEditDialogInterface =
      parentFragment as ProfileEditFragment

    val alertDialog = AlertDialog.Builder(activity as Context, R.style.OppiaAlertDialogTheme)
      .setTitle(R.string.profile_edit_delete_dialog_title)
      .setMessage(R.string.profile_edit_delete_dialog_message)
      .setNegativeButton(R.string.profile_edit_delete_dialog_negative) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.profile_edit_delete_dialog_positive) { _, _ ->
        profileEditDialogInterface.deleteProfileByInternalProfileId(internalProfileId)
      }
      .create()
    return alertDialog
  }
}

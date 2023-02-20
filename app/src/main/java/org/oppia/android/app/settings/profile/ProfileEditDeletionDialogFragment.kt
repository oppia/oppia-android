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
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.extractCurrentUserProfileId

/** [DialogFragment] that gives option to delete profile. */
class ProfileEditDeletionDialogFragment : InjectableDialogFragment() {

  companion object {

    /** Creates new instance of the fragment [ProfileEditFragment]. */
    fun newInstance(profileId: ProfileId): ProfileEditDeletionDialogFragment {
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

    val profileId = args.extractCurrentUserProfileId()

    profileEditDialogInterface =
      parentFragment as ProfileEditFragment

    val alertDialog = AlertDialog.Builder(activity as Context, R.style.OppiaAlertDialogTheme)
      .setTitle(R.string.profile_edit_delete_dialog_title)
      .setMessage(R.string.profile_edit_delete_dialog_message)
      .setNegativeButton(R.string.profile_edit_delete_dialog_negative) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.profile_edit_delete_dialog_positive) { dialog, _ ->
        profileEditDialogInterface.deleteProfileByInternalProfileId(profileId)
      }
      .create()
    return alertDialog
  }
}

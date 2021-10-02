package org.oppia.android.app.settings.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment

/** [DialogFragment] that gives option to delete profile. */
class ProfileEditDeletionDialogFragment : InjectableDialogFragment() {

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val PROFILE_DELETION_DIALOG_INTERNAL_PROFILE_ID_EXTRA_KEY =
      "ProfileEditDeletionDialogFragment.profile_deletion_dialog_internal_profile_id"

    fun newInstance(internalProfileId: Int): ProfileEditDeletionDialogFragment {
      val profileEditDeletionDialog = ProfileEditDeletionDialogFragment()
      val args = Bundle()
      args.putInt(PROFILE_DELETION_DIALOG_INTERNAL_PROFILE_ID_EXTRA_KEY, internalProfileId)
      profileEditDeletionDialog.arguments = args
      return profileEditDeletionDialog
    }
  }

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

    val internalProfileId = args.getInt(PROFILE_DELETION_DIALOG_INTERNAL_PROFILE_ID_EXTRA_KEY)

    profileEditDialogInterface =
      parentFragment as ProfileEditFragment

    val alertDialog = AlertDialog.Builder(activity as Context, R.style.AlertDialogTheme)
      .setTitle(R.string.profile_edit_delete_dialog_title)
      .setMessage(R.string.profile_edit_delete_dialog_message)
      .setNegativeButton(R.string.profile_edit_delete_dialog_negative) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.profile_edit_delete_dialog_positive) { dialog, _ ->
        profileEditDialogInterface.deleteProfileByInternalProfileId(internalProfileId)
      }
      .create()
    return alertDialog
  }
}

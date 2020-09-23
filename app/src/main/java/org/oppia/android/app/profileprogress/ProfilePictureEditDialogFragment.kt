package org.oppia.android.app.profileprogress

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.app.R

/** [DialogFragment] that gives option to either view the profile picture or change the current profile picture. */
class ProfilePictureEditDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [ProfilePictureEditDialogFragment]: DialogFragment
     */
    fun newInstance(): ProfilePictureEditDialogFragment {
      return ProfilePictureEditDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val view = View.inflate(
      context,
      R.layout.profile_picture_edit_dialog,
      /* root= */ null
    )
    val viewProfilePicture =
      view.findViewById<TextView>(R.id.profile_picture_edit_dialog_view_picture)
    val chooseFromGallery =
      view.findViewById<TextView>(R.id.profile_picture_edit_dialog_change_picture)

    val profilePictureEditDialogInterface: ProfilePictureDialogInterface =
      activity as ProfilePictureDialogInterface

    viewProfilePicture.setOnClickListener {
      profilePictureEditDialogInterface.showProfilePicture()
      dialog?.dismiss()
    }
    chooseFromGallery.setOnClickListener {
      profilePictureEditDialogInterface.showGalleryForProfilePicture()
      dialog?.dismiss()
    }
    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.profile_progress_edit_dialog_title)
      .setView(view)
      .setNegativeButton(R.string.profile_picture_edit_alert_dialog_cancel_button) { _, _ ->
        dismiss()
      }
      .create()
  }
}

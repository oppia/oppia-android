package org.oppia.android.app.mydownloads.downloads

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.mydownloads.PROFILE_DOWNLOAD_ACCESS_ARGUMENT_KEY
import org.oppia.android.app.mydownloads.PROFILE_ID_ARGUMENT_KEY

class DownloadsTopicDeleteDialogFragment : DialogFragment() {

  private lateinit var alertDialog: AlertDialog

  companion object {

    fun newInstance(
      internalProfileId: Int,
      allowedDownloadAccess: Boolean
    ): DownloadsTopicDeleteDialogFragment {
      val downloadsTopicDeleteDialogFragment = DownloadsTopicDeleteDialogFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putBoolean(PROFILE_DOWNLOAD_ACCESS_ARGUMENT_KEY, allowedDownloadAccess)
      downloadsTopicDeleteDialogFragment.arguments = args
      return downloadsTopicDeleteDialogFragment
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args =
      checkNotNull(arguments) {
        "Expected arguments to be pass to DownloadsTopicDeleteDialogFragment"
      }

    val internalProfileId = args.getInt(PROFILE_ID_ARGUMENT_KEY)
    val isAllowedDownloadAccess = args.getBoolean(PROFILE_DOWNLOAD_ACCESS_ARGUMENT_KEY)

    if (isAllowedDownloadAccess) {
      alertDialog = AlertDialog
        .Builder(ContextThemeWrapper(activity as Context, R.style.AlertDialogTheme))
        .setMessage(R.string.download_topic_delete_dialog_message)
        .setNegativeButton(R.string.download_topic_delete__dialog_cancel) { dialog, _ ->
          dialog.dismiss()
        }
        .setPositiveButton(R.string.home_activity_back_dialog_exit) { _, _ ->
          // TODO(): call delete API in DownloadManagementController
        }
        .create()
    } else {
    }

    alertDialog.setCanceledOnTouchOutside(false)
    return alertDialog
  }
}

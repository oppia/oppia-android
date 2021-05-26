package org.oppia.android.app.mydownloads.downloads

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.R

/** [DialogFragment] that gives option to either cancel or delete the downloaded topic. */
class DownloadsTopicDeleteDialogFragment : DialogFragment() {

  companion object {
    fun newInstance(
      internalProfileId: Int,
      allowedDownloadAccess: Boolean
    ): DownloadsTopicDeleteDialogFragment {
      val downloadsTopicDeleteDialogFragment = DownloadsTopicDeleteDialogFragment()
      val args = Bundle()
      args.putInt(DownloadsFragment.INTERNAL_PROFILE_ID_SAVED_KEY, internalProfileId)
      args.putBoolean(DownloadsFragment.IS_ALLOWED_DOWNLOAD_ACCESS_SAVED_KEY, allowedDownloadAccess)
      downloadsTopicDeleteDialogFragment.arguments = args
      return downloadsTopicDeleteDialogFragment
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args =
      checkNotNull(arguments) {
        "Expected arguments to be pass to DownloadsTopicDeleteDialogFragment"
      }

    // TODO(#3068): keeping these values if the controller required else can be removed
    val internalProfileId = args.getInt(DownloadsFragment.INTERNAL_PROFILE_ID_SAVED_KEY)
    val isAllowedDownloadAccess =
      args.getBoolean(DownloadsFragment.IS_ALLOWED_DOWNLOAD_ACCESS_SAVED_KEY)

    val alertDialog = AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.AlertDialogTheme))
      .setMessage(R.string.downloads_topic_delete_dialog_message)
      .setNegativeButton(R.string.downloads_topic_delete_dialog_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.downloads_topic_delete_dialog_delete) { _, _ ->
        // TODO(#3068): call delete API in DownloadManagementController
      }
      .create()

    alertDialog.setCanceledOnTouchOutside(false)
    return alertDialog
  }
}

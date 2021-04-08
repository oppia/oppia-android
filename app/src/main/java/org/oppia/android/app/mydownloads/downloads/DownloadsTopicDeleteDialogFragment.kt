package org.oppia.android.app.mydownloads.downloads

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.mydownloads.INTERNAL_PROFILE_ID_SAVED_KEY
import org.oppia.android.app.mydownloads.IS_ALLOWED_DOWNLOAD_ACCESS_SAVED_KEY

/** [DialogFragment] that gives option to either cancel or delete the downloaded topic. */
class DownloadsTopicDeleteDialogFragment : DialogFragment() {

  companion object {

    fun newInstance(
      internalProfileId: Int,
      allowedDownloadAccess: Boolean
    ): DownloadsTopicDeleteDialogFragment {
      val downloadsTopicDeleteDialogFragment = DownloadsTopicDeleteDialogFragment()
      val args = Bundle()
      args.putInt(INTERNAL_PROFILE_ID_SAVED_KEY, internalProfileId)
      args.putBoolean(IS_ALLOWED_DOWNLOAD_ACCESS_SAVED_KEY, allowedDownloadAccess)
      downloadsTopicDeleteDialogFragment.arguments = args
      return downloadsTopicDeleteDialogFragment
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args =
      checkNotNull(arguments) {
        "Expected arguments to be pass to DownloadsTopicDeleteDialogFragment"
      }

    // TODO(#552): remove if not needed
    val internalProfileId = args.getInt(INTERNAL_PROFILE_ID_SAVED_KEY)
    val isAllowedDownloadAccess = args.getBoolean(IS_ALLOWED_DOWNLOAD_ACCESS_SAVED_KEY)

    val alertDialog = AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.AlertDialogTheme))
      .setMessage(R.string.download_topic_delete_dialog_message)
      .setNegativeButton(R.string.download_topic_delete__dialog_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.download_topic_delete__dialog_delete) { _, _ ->
        // TODO(): call delete API in DownloadManagementController
      }
      .create()

    alertDialog.setCanceledOnTouchOutside(false)
    return alertDialog
  }
}

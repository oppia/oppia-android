package org.oppia.android.app.topic.info

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.R

/**
 * DialogFragment that indicates the networking detail for topic download.
 */
class TopicInfoDownloadDialogFragment : DialogFragment() {

  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [TopicInfoDownloadDialogFragment]: DialogFragment
     */
    fun newInstance(
      title: Int,
      message: Int,
      positiveButtonText: Int
    ): TopicInfoDownloadDialogFragment {
      val topicInfoDownloadDialogFragment = TopicInfoDownloadDialogFragment()
      val args = Bundle()
      args.putInt("title", title)
      args.putInt("message", message)
      args.putInt("positiveButtonText", positiveButtonText)
      topicInfoDownloadDialogFragment.arguments = args
      return topicInfoDownloadDialogFragment
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

    val args =
      checkNotNull(arguments) { "Expected arguments to be pass to TopicInfoDownloadDialogFragment" }

    val title = args.getInt("title")
    val message = args.getInt("message")
    val positiveButtonText = args.getInt("positiveButtonText")

    val view = View.inflate(context, R.layout.cellular_data_dialog, /* root= */ null)
    val checkBox = view.findViewById<CheckBox>(R.id.cellular_data_dialog_checkbox)

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(activity!!.getString(title))
      .setView(view)
      .setMessage(activity!!.getString(message))
      .setPositiveButton(activity!!.getString(positiveButtonText)) { _, _ ->
        checkBox.isChecked
        dismiss()
      }
      .setNegativeButton(R.string.cellular_data_alert_dialog_cancel_button) { _, _ ->
        checkBox.isChecked
        dismiss()
      }
      .create()
  }
}

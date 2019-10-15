package org.oppia.app.topic.overview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.widget.CheckBox
import org.oppia.app.R

/**
 * DialogFragment that indicates to the user they are on cellular when trying to play an audio voiceover.
 */
class TopicDownloadDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [TopicDownloadDialogFragment]: DialogFragment
     */
    fun newInstance(): TopicDownloadDialogFragment {
      return TopicDownloadDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val view = activity!!.layoutInflater.inflate(R.layout.topic_download_dialog, null)
    val checkBox = view.findViewById<CheckBox>(R.id.topic_download_dialog_checkbox)

    val topicDownloadListener: TopicDownloadListener = parentFragment as TopicOverviewFragment

    return AlertDialog.Builder(activity as Context)
      .setTitle(R.string.topic_download_alert_dialog_title)
      .setView(view)
      .setMessage(R.string.topic_download_alert_dialog_description)
      .setPositiveButton(R.string.topic_download_alert_dialog_download_button) { dialog, whichButton ->
        topicDownloadListener.downloadTopicWhileOnCellular(checkBox.isChecked)
        dismiss()
      }
      .setNegativeButton(R.string.topic_download_alert_dialog_cancel_button) { dialog, whichButton ->
        topicDownloadListener.doNotDownloadTopicWhileOnCellular(checkBox.isChecked)
        dismiss()
      }
      .create()
  }
}

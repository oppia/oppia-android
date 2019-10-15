package org.oppia.app.topic.overview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.app.R

/**
 * DialogFragment that confirms whether user wants to delete the topic or not.
 */
class TopicDeleteDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [TopicDeleteDialogFragment]: DialogFragment
     */
    fun newInstance(): TopicDeleteDialogFragment {
      return TopicDeleteDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val topicDeleteListener: TopicDeleteListener = parentFragment as TopicOverviewFragment

    return AlertDialog.Builder(activity as Context)
      .setMessage(R.string.topic_delete_alert_dialog_description)
      .setPositiveButton(R.string.topic_delete_alert_dialog_delete_button) { dialog, whichButton ->
        topicDeleteListener.deleteTopic()
        dismiss()
      }
      .setNegativeButton(R.string.topic_delete_alert_dialog_cancel_button) { dialog, whichButton ->
        dismiss()
      }
      .create()
  }
}

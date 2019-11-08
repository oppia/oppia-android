package org.oppia.app.player.stopexploration

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.app.R
import org.oppia.app.player.exploration.ExplorationActivity

/**
 * DialogFragment that gives option to learner to stop exploration in between.
 */
class StopExplorationDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [StopExplorationDialogFragment]: DialogFragment
     */
    fun newInstance(): StopExplorationDialogFragment {
      return StopExplorationDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val stopExplorationInterface: StopExplorationInterface = activity as ExplorationActivity

    return AlertDialog.Builder(activity as Context)
      .setTitle(R.string.stop_exploration_dialog_title)
      .setMessage(R.string.stop_exploration_dialog_description)
      .setPositiveButton(R.string.stop_exploration_dialog_leave_button) { dialog, whichButton ->
        stopExplorationInterface.stopExploration()
        dismiss()
      }
      .setNegativeButton(R.string.stop_exploration_dialog_cancel_button) { dialog, whichButton ->
        dismiss()
      }
      .create()
  }
}

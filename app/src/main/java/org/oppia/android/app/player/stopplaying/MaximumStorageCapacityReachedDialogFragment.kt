package org.oppia.android.app.player.stopplaying

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.R

/**
 * DialogFragment that gives option to learner to stop exploration in between.
 */
class MaximumStorageCapacityReachedDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [MaximumStorageCapacityReachedDialogFragment]: DialogFragment
     */
    fun newInstance(): MaximumStorageCapacityReachedDialogFragment {
      return MaximumStorageCapacityReachedDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val stopStatePlayingSessionListener: StopStatePlayingSessionListener =
      activity as StopStatePlayingSessionListener

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.stop_exploration_dialog_title)
      .setMessage(R.string.stop_exploration_dialog_description)
      .setPositiveButton(R.string.stop_exploration_dialog_leave_button) { _, _ ->
        stopStatePlayingSessionListener.stopSession()
        dismiss()
      }
      .setNegativeButton(R.string.stop_exploration_dialog_cancel_button) { _, _ ->
        dismiss()
      }
      .create()
  }
}

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
class UnsavedExplorationDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [UnsavedExplorationDialogFragment]: DialogFragment
     */
    fun newInstance(): UnsavedExplorationDialogFragment {
      return UnsavedExplorationDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val stopStatePlayingSessionWithSavedProgressListener:
      StopStatePlayingSessionWithSavedProgressListener =
        activity as StopStatePlayingSessionWithSavedProgressListener

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.unsaved_exploration_dialog_title)
      .setMessage(R.string.unsaved_exploration_dialog_description)
      .setPositiveButton(R.string.unsaved_exploration_dialog_leave_button) { _, _ ->
        stopStatePlayingSessionWithSavedProgressListener
          .deleteCurrentProgressAndStopSession()
        dismiss()
      }
      .setNegativeButton(R.string.unsaved_exploration_dialog_cancel_button) { _, _ ->
        dismiss()
      }
      .create()
  }
}

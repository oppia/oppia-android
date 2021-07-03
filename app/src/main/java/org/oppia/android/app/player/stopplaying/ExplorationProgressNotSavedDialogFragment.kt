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
class ExplorationProgressNotSavedDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [ExplorationProgressNotSavedDialogFragment]: DialogFragment
     */
    fun newInstance(): ExplorationProgressNotSavedDialogFragment {
      return ExplorationProgressNotSavedDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val stopStatePlayingSessionWithSavedProgressListener:
      StopStatePlayingSessionWithSavedProgressListener =
        activity as StopStatePlayingSessionWithSavedProgressListener

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.exploration_progress_not_saved_dialog_title)
      .setMessage(R.string.exploration_progress_not_saved_dialog_description)
      .setPositiveButton(R.string.exploration_progress_not_saved_dialog_leave_button) { _, _ ->
        stopStatePlayingSessionWithSavedProgressListener.deleteCurrentProgressStopCurrentSession()
        dismiss()
      }
      .setNegativeButton(R.string.exploration_progress_not_saved_dialog_cancel_button) { _, _ ->
        dismiss()
      }
      .create()
  }
}

package org.oppia.android.app.player.stopplaying

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment

/**
 * DialogFragment that visible to the user when they exit a partially complete exploration with
 * unsaved progress.
 */
class UnsavedExplorationDialogFragment : InjectableDialogFragment() {
  companion object {
    /**
     * Responsible for displaying content in DialogFragment.
     *
     * @return [UnsavedExplorationDialogFragment]: DialogFragment
     */
    fun newInstance(): UnsavedExplorationDialogFragment {
      return UnsavedExplorationDialogFragment()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (fragmentComponent as FragmentComponentImpl).inject(this)
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
          .deleteCurrentProgressAndStopSession(isCompletion = false)
        dismiss()
      }
      .setNegativeButton(R.string.unsaved_exploration_dialog_cancel_button) { _, _ ->
        dismiss()
      }
      .create()
  }
}

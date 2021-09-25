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
 * DialogFragment that is visible to the user when they exit a partially complete exploration
 * if the exploration has saved progress and the checkpoint database has not exceeded the allocated
 * limit.
 */
class StopExplorationDialogFragment : InjectableDialogFragment() {
  companion object {
    /**
     * Responsible for displaying content in DialogFragment.
     *
     * @return [StopExplorationDialogFragment]: DialogFragment
     */
    fun newInstance(): StopExplorationDialogFragment {
      return StopExplorationDialogFragment()
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
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

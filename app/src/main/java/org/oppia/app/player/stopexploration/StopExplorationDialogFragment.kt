package org.oppia.app.player.stopexploration

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.app.R
import org.oppia.app.model.StoryTextSize

import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.utility.FontScaleConfigurationUtil

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

    return AlertDialog.Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.stop_exploration_dialog_title)
      .setMessage(R.string.stop_exploration_dialog_description)
      .setPositiveButton(R.string.stop_exploration_dialog_leave_button) { _, _ ->
        FontScaleConfigurationUtil.adjustFontSize(activity as Context, StoryTextSize.MEDIUM_TEXT_SIZE)
        stopExplorationInterface.stopExploration()
        dismiss()
      }
      .setNegativeButton(R.string.stop_exploration_dialog_cancel_button) { _, _ ->
        dismiss()
      }
      .create()
  }
}

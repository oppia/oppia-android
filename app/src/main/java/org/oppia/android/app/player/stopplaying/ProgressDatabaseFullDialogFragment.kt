package org.oppia.android.app.player.stopplaying

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.android.R

private const val OLDEST_SAVED_EXPLORATION_TITLE_ARGUMENT_KEY =
  "MaximumStorageCapacityReachedDialogFragment.oldest_saved_exploration_title"

/**
 * DialogFragment that is visible to the learner when they exit a partially complete
 * exploration if the exploration has saved progress and the checkpoint database has exceeded the
 * allocated limit.
 *
 * This dialog fragment gives the user the option to either overwrite the oldest saved progress with
 * the current progress, leave the exploration without saving the current progress, or go back to
 * continue the current exploration.
 */
class ProgressDatabaseFullDialogFragment : DialogFragment() {
  companion object {
    /**
     * Responsible for displaying content in DialogFragment.
     *
     * @return [ProgressDatabaseFullDialogFragment]: DialogFragment
     */
    fun newInstance(
      oldestSavedExplorationTitle: String
    ): ProgressDatabaseFullDialogFragment {
      val maximumStorageCapacityReachedDialogFragment =
        ProgressDatabaseFullDialogFragment()
      val args = Bundle()
      args.putString(OLDEST_SAVED_EXPLORATION_TITLE_ARGUMENT_KEY, oldestSavedExplorationTitle)
      maximumStorageCapacityReachedDialogFragment.arguments = args
      return maximumStorageCapacityReachedDialogFragment
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val oldestSavedExplorationTitle = arguments
      ?.getString(OLDEST_SAVED_EXPLORATION_TITLE_ARGUMENT_KEY)
    val stopStatePlayingSessionListenerWithSavedProgressListener:
      StopStatePlayingSessionWithSavedProgressListener =
        activity as StopStatePlayingSessionWithSavedProgressListener

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.progress_database_full_dialog_title)
      .setMessage(
        getString(R.string.progress_database_full_dialog_description, oldestSavedExplorationTitle)
      )
      .setPositiveButton(R.string.progress_database_full_dialog_continue_button) { _, _ ->
        stopStatePlayingSessionListenerWithSavedProgressListener
          .deleteOldestProgressAndStopSession()
        dismiss()
      }
      .setNeutralButton(
        R.string.progress_database_full_dialog_leave_without_saving_progress_button
      ) { _, _ ->
        stopStatePlayingSessionListenerWithSavedProgressListener
          .deleteCurrentProgressAndStopSession()
        dismiss()
      }
      .setNegativeButton(
        R.string.progress_database_full_dialog_back_to_lesson_button
      ) { _, _ ->
        dismiss()
      }
      .create()
  }
}

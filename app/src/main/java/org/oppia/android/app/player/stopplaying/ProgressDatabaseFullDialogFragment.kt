package org.oppia.android.app.player.stopplaying

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.ProgressDatabaseFullDialogFragmentArguments
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/**
 * DialogFragment that is visible to the learner when they exit a partially complete
 * exploration if the exploration has saved progress and the checkpoint database has exceeded the
 * allocated limit.
 *
 * This dialog fragment gives the user the option to either overwrite the oldest saved progress with
 * the current progress, leave the exploration without saving the current progress, or go back to
 * continue the current exploration.
 */
class ProgressDatabaseFullDialogFragment : InjectableDialogFragment() {
  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  companion object {
    /** Arguments key for ProgressDatabaseFullDialogFragment. */
    const val PROGRESS_DATABASE_FULL_DIALOG_FRAGMENT_ARGUMENTS_KEY =
      "ProgressDatabaseFullDialogFragment.arguments"

    /**
     * Responsible for displaying content in DialogFragment.
     *
     * @return [ProgressDatabaseFullDialogFragment]: DialogFragment
     */
    fun newInstance(
      oldestSavedExplorationTitle: String
    ): ProgressDatabaseFullDialogFragment {
      val args = ProgressDatabaseFullDialogFragmentArguments.newBuilder()
        .setOldestSavedExplorationTitle(oldestSavedExplorationTitle).build()
      return ProgressDatabaseFullDialogFragment().apply {
        arguments = Bundle().apply {
          putProto(PROGRESS_DATABASE_FULL_DIALOG_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val arguments = checkNotNull(arguments) { "Expected arguments to be passed to dialog fragment" }
    val oldestSavedExplorationTitle =
      arguments.getProto(
        PROGRESS_DATABASE_FULL_DIALOG_FRAGMENT_ARGUMENTS_KEY,
        ProgressDatabaseFullDialogFragmentArguments.getDefaultInstance()
      )
        .oldestSavedExplorationTitle
        ?: error("Expected exploration title to be passed via arguments")
    val stopStatePlayingSessionListenerWithSavedProgressListener:
      StopStatePlayingSessionWithSavedProgressListener =
        activity as StopStatePlayingSessionWithSavedProgressListener

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.progress_database_full_dialog_title)
      .setMessage(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.progress_database_full_dialog_description, oldestSavedExplorationTitle
        )
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
          .deleteCurrentProgressAndStopSession(isCompletion = false)
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

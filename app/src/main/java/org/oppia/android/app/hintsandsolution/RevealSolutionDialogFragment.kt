package org.oppia.android.app.hintsandsolution

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject

/**
 * DialogFragment that asks to the user if they want to reveal solution.
 */
class RevealSolutionDialogFragment : InjectableDialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [RevealSolutionDialogFragment]: DialogFragment
     */
    fun newInstance(): RevealSolutionDialogFragment {
      return RevealSolutionDialogFragment()
    }
  }

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val view = View.inflate(context, R.layout.reveal_solution_dialog, /* root= */ null)
    val revealSolutionInterface: RevealSolutionInterface =
      parentFragment as HintsAndSolutionDialogFragment

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.hint_solution_dialog_reveal_solution_text)
      .setView(view)
      .setMessage(
        resourceHandler.getStringInLocale(
          R.string.hint_solution_dialog_reveal_dialog_solution_text
        )
      )
      .setPositiveButton(
        resourceHandler.getStringInLocale(
          R.string.hint_solution_dialog_reveal_dialog_button_text
        )
      ) { _, _ ->
        revealSolutionInterface.revealSolution()
        dismiss()
      }
      .setNegativeButton(R.string.cellular_audio_dialog_cancel_button_text) { _, _ ->
        dismiss()
      }
      .create()
  }
}

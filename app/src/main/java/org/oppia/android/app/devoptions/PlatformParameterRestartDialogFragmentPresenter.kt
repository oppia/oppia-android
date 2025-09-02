package org.oppia.android.app.devoptions

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.RestartDialogFragmentBinding
import org.oppia.android.app.ui.R
import javax.inject.Inject

/** Presenter for the [PlatformParameterRestartDialogFragment]. */
class PlatformParameterRestartDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
) {

  /**
   * Creates a dialog that prompts the user to restart the app when platform parameters are
   * updated.
   */
  fun handleOnCreateDialog(): Dialog {
    val binding = RestartDialogFragmentBinding.inflate(
      activity.layoutInflater,
      /* parent= */ null,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment

    val dialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setView(binding.root)
      .create()
    dialog.setCanceledOnTouchOutside(false)

    binding.restartButton.setOnClickListener {
      dialog.dismiss()
      activity.finishAffinity()
    }
    return dialog
  }
}

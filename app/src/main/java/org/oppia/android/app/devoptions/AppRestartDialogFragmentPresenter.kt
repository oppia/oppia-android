package org.oppia.android.app.devoptions

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.AppRestartDialogFragmentBinding
import org.oppia.android.app.ui.R
import javax.inject.Inject

/** Presenter for the [AppRestartDialogFragment]. */
class AppRestartDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
) {
  /** Creates a dialog that prompts the user to perform a full app restart. */
  fun handleOnCreateDialog(): Dialog {
    val binding = AppRestartDialogFragmentBinding.inflate(
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

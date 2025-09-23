package org.oppia.android.app.devoptions

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.PendingChangesDialogFragmentBinding
import org.oppia.android.app.ui.R
import javax.inject.Inject

/** Presenter for the [PendingChangesDialogFragment]. */
class PendingChangesDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
) {
  /** Creates a dialog that prompts the user to save or discard the changes in the dashboard. */
  fun handleOnCreateDialog(): Dialog {
    val binding = PendingChangesDialogFragmentBinding.inflate(
      activity.layoutInflater,
      /* parent= */ null,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment

    val pendingChangesInterface = fragment.parentFragment as SavePendingChangesDialogListener
    val dialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setView(binding.root)
      .create()
    dialog.setCanceledOnTouchOutside(true)

    binding.saveButton.setOnClickListener {
      pendingChangesInterface.savePendingChanges()
      dialog.dismiss()
    }

    binding.discardButton.setOnClickListener {
      pendingChangesInterface.discardPendingChanges()
      dialog.dismiss()
    }
    return dialog
  }
}

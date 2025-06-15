package org.oppia.android.app.flashback

import android.app.Dialog
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import javax.inject.Inject
import org.oppia.android.app.ui.R
import org.oppia.android.app.databinding.databinding.FlashbackConfirmationDialogFragmentBinding
import org.oppia.android.domain.exploration.ExplorationProgressController

const val TAG_FLASHBACK_CONFIRMATION_DIALOG = "FLASHBACK_CONFIRMATION_DIALOG"

/** Presenter for [FlashbackConfirmationDialogFragment], sets up bindings from ViewModel. */
@FragmentScope
class FlashbackConfirmationDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val explorationProgressController: ExplorationProgressController,
) {
  fun handleOnCreateDialog(stateName: String): Dialog {
    val binding = FlashbackConfirmationDialogFragmentBinding.inflate(
      LayoutInflater.from(activity),
      null,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment

    val dialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setView(binding.root)
      .create()

    dialog.setCanceledOnTouchOutside(false)

    binding.continueConfirmationButton.setOnClickListener {
      explorationProgressController.moveToFlashback(stateName)
      dialog.dismiss()
    }
    binding.notNowButton.setOnClickListener {
      dialog.dismiss()
    }
    return dialog
  }
}
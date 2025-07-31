package org.oppia.android.app.flashback

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.FlashbackConfirmationDialogFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.ui.R
import org.oppia.android.domain.exploration.ExplorationProgressController
import javax.inject.Inject

/** Tag for displaying the [FlashbackConfirmationDialogFragment]. */
const val TAG_FLASHBACK_CONFIRMATION_DIALOG = "FLASHBACK_CONFIRMATION_DIALOG"

/** Presenter for [FlashbackConfirmationDialogFragment]. */
@FragmentScope
class FlashbackConfirmationDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val explorationProgressController: ExplorationProgressController
) {

  /** Sets up data binding. */
  fun handleOnCreateDialog(stateName: String, isFlashbackViewed: Boolean): Dialog {
    val binding = FlashbackConfirmationDialogFragmentBinding.inflate(
      activity.layoutInflater,
      /* parent= */ null,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment

    val dialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setView(binding.root)
      .create()
    dialog.setCanceledOnTouchOutside(false)

    binding.continueConfirmationButton.setOnClickListener {
      explorationProgressController.moveToFlashback(stateName, isFlashbackViewed)
      dialog.dismiss()
    }

    binding.notNowButton.setOnClickListener {
      dialog.dismiss()
    }

    return dialog
  }
}

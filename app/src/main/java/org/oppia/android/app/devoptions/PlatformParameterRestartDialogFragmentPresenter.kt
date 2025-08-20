package org.oppia.android.app.devoptions

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.RestartDialogFragmentBinding
import org.oppia.android.app.splash.SplashActivity
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
    dialog.setCanceledOnTouchOutside(true)

    binding.restartButton.setOnClickListener {
      val intent = Intent(activity, SplashActivity::class.java).also {
        it.action = Intent.ACTION_MAIN
        it.addCategory(Intent.CATEGORY_LAUNCHER)
      }
      dialog.dismiss()
      activity.finishAffinity()
      activity.startActivity(intent)
    }
    return dialog
  }
}

package org.oppia.android.app.devoptions

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.ForceDownloadDialogFragmentBinding
import org.oppia.android.app.ui.R
import org.oppia.android.domain.devoptions.ForceDownloadParametersController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** Presenter for the [ForceDownloadDialogFragment]. */
class ForceDownloadDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger
) {
  /** Creates a dialog that prompts the user to perform a full app restart. */
  fun handleOnCreateDialog(
    forceDownloadParametersController: ForceDownloadParametersController
  ): Dialog {
    val binding = ForceDownloadDialogFragmentBinding.inflate(
      activity.layoutInflater,
      /* parent= */ null,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment

    forceDownloadParametersController.downloadRemoteParameters().toLiveData().observe(fragment) {
      when (it) {
        is AsyncResult.Success -> {
          oppiaLogger.d(
            "ForceDownloadDialog",
            "Remote parameters downloaded successfully."
          )
        }
        is AsyncResult.Failure -> { }
        is AsyncResult.Pending -> { }
      }
    }

    val dialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setView(binding.root)
      .create()
    dialog.setCanceledOnTouchOutside(false)

    binding.restartButton.setOnClickListener {
      dialog.dismiss()
      activity.finishAffinity()
    }
    binding.cancelButton.setOnClickListener {
      forceDownloadParametersController.setForceDownloadEnabled(true)
      dialog.dismiss()
    }
    return dialog
  }
}

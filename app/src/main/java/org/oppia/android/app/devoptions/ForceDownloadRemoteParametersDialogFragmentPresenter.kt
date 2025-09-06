package org.oppia.android.app.devoptions

import android.app.AlertDialog
import android.app.Dialog
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.ForceDownloadRemoteParametersDialogFragmentBinding
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.devoptions.ForceDownloadRemoteParametersController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** Presenter for the [ForceDownloadRemoteParametersDialogFragment]. */
class ForceDownloadRemoteParametersDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler,
  private val forceDownloadRemoteParametersParametersController:
    ForceDownloadRemoteParametersController
) {
  /**
   *  Creates a dialog to display the status of the ongoing remote parameters download and then
   *  restart.
   */
  fun handleOnCreateDialog(): Dialog {
    val binding = ForceDownloadRemoteParametersDialogFragmentBinding.inflate(
      activity.layoutInflater,
      /* parent= */ null,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment
    binding.isRestartEnabled = false

    forceDownloadRemoteParametersParametersController
      .downloadRemoteParameters().toLiveData().observe(fragment) {
        when (it) {
          is AsyncResult.Success -> {
            oppiaLogger.d(
              "ForceDownloadRemoteParametersDialog",
              "Remote parameters downloaded successfully."
            )
            handleDownloadComplete(binding)
          }
          is AsyncResult.Failure -> {}
          is AsyncResult.Pending -> {}
          else -> {} // do nothing
        }
      }

    val dialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setView(binding.root)
      .create()
    dialog.setCanceledOnTouchOutside(false)

    binding.restartButton.setOnClickListener {
      (activity as DeveloperOptionsActivity).developerOptionsActivityPresenter.markRestartRequired()
      dialog.dismiss()
      activity.finishAffinity()
    }
    binding.cancelButton.setOnClickListener {
      forceDownloadRemoteParametersParametersController.cancelRemoteParameterDownload()
      dialog.dismiss()
    }
    return dialog
  }

  private fun handleDownloadComplete(binding: ForceDownloadRemoteParametersDialogFragmentBinding) {
    binding.cancelButton.visibility = View.GONE
    binding.forceDownloadMessage.text = resourceHandler
      .getStringInLocale(R.string.force_download_dialog_successfully_downloaded_message_text)
    binding.forceDownloadRestartMessage.visibility = View.VISIBLE
    binding.isRestartEnabled = true
  }
}

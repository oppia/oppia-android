package org.oppia.android.app.devoptions

import android.app.AlertDialog
import android.app.Dialog
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    val appRestartInterface = activity as AppRestartListener

    forceDownloadRemoteParametersParametersController
      .downloadRemoteParameters().toLiveData().observe(fragment) {
        when (it) {
          is AsyncResult.Pending -> {} // Do nothing.
          is AsyncResult.Success -> {
            oppiaLogger.d(
              "ForceDownloadRemoteParametersDialog",
              "Remote parameters downloaded successfully."
            )
            handleDownloadComplete(binding)
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "ForceDownloadRemoteParametersDialog", "Failed to download parameters.", it.error
            )
            handleDownloadFailed(binding)
          }
        }
      }

    val dialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setView(binding.root)
      .create()
    dialog.setCanceledOnTouchOutside(false)

    binding.restartButton.setOnClickListener {
      appRestartInterface.restartApp()
      dialog.dismiss()
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
    binding.restartButton.apply {
      isEnabled = true
      setTextColor(
        ContextCompat.getColor(
          fragment.requireContext(),
          R.color.component_color_shared_secondary_4_text_color
        )
      )
      setBackgroundResource(R.drawable.state_button_primary_background)
    }
  }

  private fun handleDownloadFailed(binding: ForceDownloadRemoteParametersDialogFragmentBinding) {
    binding.forceDownloadMessage.text =
      resourceHandler.getStringInLocale(R.string.force_download_dialog_failed_message_text)
    binding.restartButton.visibility = View.GONE
  }
}

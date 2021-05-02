package org.oppia.android.app.mydownloads.downloads

import android.app.Dialog
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.utility.AlertDialogHelper
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.DownloadsAccessDialogBinding
import javax.inject.Inject

/** The presenter for [DownloadsAccessDialogFragment]. */
@FragmentScope
class DownloadsAccessDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<DownloadsAccessViewModel>
) {

  private val downloadsTopicDeleteInterface =
    fragment.parentFragment as DownloadsTopicDeleteInterface
  private val downloadsAccessAllowedViewModel by lazy {
    getDownloadsAccessViewModel()
  }

  fun handleOnCreateDialog(
    adminPin: String?,
    allowDownloadAccess: Boolean
  ): Dialog {
    val binding: DownloadsAccessDialogBinding =
      DataBindingUtil.inflate(
        activity.layoutInflater,
        R.layout.downloads_access_dialog,
        /* parent= */ null,
        /* attachToParent= */ false
      )

    binding.apply {
      lifecycleOwner = fragment
      viewModel = downloadsAccessAllowedViewModel
    }

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.downloadsAccessInputPinEditText.onTextChanged { confirmPin ->
      confirmPin?.let {
        if (
          downloadsAccessAllowedViewModel.errorMessage.get()
            ?.isNotEmpty()!! && downloadsAccessAllowedViewModel.inputPin.get() == it
        ) {
          downloadsAccessAllowedViewModel.inputPin.set(it)
        } else {
          downloadsAccessAllowedViewModel.inputPin.set(it)
          downloadsAccessAllowedViewModel.errorMessage.set("")
        }
      }
    }

    val dialog = AlertDialogHelper.getAlertDialog(
      context = activity,
      view = binding.root,
      title = R.string.admin_settings_heading,
      message = R.string.admin_settings_sub,
      positiveButtonText = R.string.admin_settings_submit,
      negativeButtonText = R.string.admin_settings_cancel
    ) { dialog, buttonId ->
      when (buttonId) {
        AlertDialog.BUTTON_POSITIVE -> {
          when {
            binding.downloadsAccessInputPinEditText.text.toString() == adminPin -> {
              dialog.dismiss()
              downloadsTopicDeleteInterface.showDownloadsTopicDeleteDialogFragment(
                allowDownloadAccess
              )
            }
            binding.downloadsAccessInputPinEditText.text.toString().isNotEmpty() -> {
              downloadsAccessAllowedViewModel.errorMessage.set(
                fragment.resources.getString(
                  R.string.downloads_access_dialog_input_pin_error
                )
              )
            }
            else -> {
              downloadsAccessAllowedViewModel.errorMessage.set(
                fragment.resources.getString(
                  R.string.downloads_access_dialog_input_pin_empty_error
                )
              )
            }
          }
        }
        AlertDialog.BUTTON_NEGATIVE -> {
          dialog.dismiss()
        }
      }
    }

    binding.downloadsAccessInputPinEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick()
      }
      false
    }
    return dialog
  }

  private fun getDownloadsAccessViewModel(): DownloadsAccessViewModel {
    return viewModelProvider.getForFragment(fragment, DownloadsAccessViewModel::class.java)
  }
}

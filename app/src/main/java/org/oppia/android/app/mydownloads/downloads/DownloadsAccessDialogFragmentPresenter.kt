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

    val dialog = AlertDialog.Builder(fragment.requireContext(), R.style.AlertDialogTheme)
      .setTitle(R.string.downloads_access_dialog_heading)
      .setView(binding.root)
      .setMessage(R.string.downloads_access_dialog_message)
      .setPositiveButton(R.string.admin_settings_submit, null)
      .setNegativeButton(R.string.admin_settings_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .create()

    binding.downloadsAccessInputPinEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick()
      }
      false
    }

    // This logic prevents the dialog from being dismissed. https://stackoverflow.com/a/7636468.
    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        if (binding.downloadsAccessInputPinEditText.text?.isEmpty()!!) {
          return@setOnClickListener
        }
        if (binding.downloadsAccessInputPinEditText.text.toString() == adminPin) {
          dialog.dismiss()
          downloadsTopicDeleteInterface.showDownloadsTopicDeleteDialogFragment(allowDownloadAccess)
        } else {
          downloadsAccessAllowedViewModel.errorMessage.set(
            fragment.resources.getString(
              R.string.admin_settings_incorrect
            )
          )
        }
      }
    }
    return dialog
  }

  private fun getDownloadsAccessViewModel(): DownloadsAccessViewModel {
    return viewModelProvider.getForFragment(fragment, DownloadsAccessViewModel::class.java)
  }
}

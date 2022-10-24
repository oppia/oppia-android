package org.oppia.android.app.profile

import android.app.Dialog
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.AdminSettingsDialogBinding
import javax.inject.Inject

/** The presenter for [AdminSettingsDialogFragment]. */
@FragmentScope
class AdminSettingsDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<AdminSettingsViewModel>,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private val adminViewModel by lazy {
    getAdminSettingsViewModel()
  }

  fun handleOnCreateDialog(
    routeDialogInterface: ProfileRouteDialogInterface,
    adminPin: String?
  ): Dialog {
    val binding: AdminSettingsDialogBinding =
      DataBindingUtil.inflate(
        activity.layoutInflater,
        R.layout.admin_settings_dialog,
        /* parent= */ null,
        /* attachToParent= */ false
      )
    binding.apply {
      lifecycleOwner = fragment
      viewModel = adminViewModel
    }

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.adminSettingsInputPinEditText.onTextChanged { confirmPin ->
      confirmPin?.let {
        if (
          adminViewModel.errorMessage.get()?.isNotEmpty()!! && adminViewModel.inputPin.get() == it
        ) {
          adminViewModel.inputPin.set(it)
        } else {
          adminViewModel.inputPin.set(it)
          adminViewModel.errorMessage.set("")
        }
      }
    }

    val dialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setTitle(R.string.admin_settings_dialog_fragment_admin_settings_heading)
      .setView(binding.root)
      .setMessage(R.string.admin_settings_dialog_fragment_admin_settings_message_text)
      .setPositiveButton(R.string.admin_settings_submit, null)
      .setNegativeButton(R.string.admin_settings_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .create()

    binding.adminSettingsInputPinEditText.setOnEditorActionListener { _, actionId, event ->
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
        if (binding.adminSettingsInputPinEditText.text?.isEmpty()!!) {
          adminViewModel.errorMessage.set(
            resourceHandler.getStringInLocale(
              R.string.admin_auth_activity_admin_auth_null_pin_error_message
            )
          )
          return@setOnClickListener
        }
        if (binding.adminSettingsInputPinEditText.text.toString() == adminPin) {
          routeDialogInterface.routeToResetPinDialog()
        } else {
          adminViewModel.errorMessage.set(
            resourceHandler.getStringInLocale(
              R.string.admin_settings_incorrect
            )
          )
        }
      }
    }
    return dialog
  }

  private fun getAdminSettingsViewModel(): AdminSettingsViewModel {
    return viewModelProvider.getForFragment(fragment, AdminSettingsViewModel::class.java)
  }
}

package org.oppia.app.profile

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.AdminSettingsDialogBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [AdminSettingsDialogFragment]. */
@FragmentScope
class AdminSettingsDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<AdminSettingsViewModel>
) {
  private val adminViewModel by lazy {
    getAdminSettingsViewModel()
  }

  fun handleOnCreateDialog(routeDialogInterface: ProfileRouteDialogInterface): Dialog {
    val adminPin = fragment.arguments?.getString(KEY_ADMIN_SETTINGS_PIN)
    checkNotNull(adminPin) { "Admin Pin must not be null" }
    val binding: AdminSettingsDialogBinding = DataBindingUtil.inflate(activity.layoutInflater, R.layout.admin_settings_dialog, null, false)
    binding.apply {
      lifecycleOwner = fragment
      viewModel = adminViewModel
    }

    binding.inputPin.addTextChangedListener(object: TextWatcher {
      override fun onTextChanged(confirmPin: CharSequence?, start: Int, before: Int, count: Int) {
        confirmPin?.let {
          adminViewModel.errorMessage.set("")
        }
      }
      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    val dialog = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setTitle(R.string.admin_settings_heading)
      .setView(binding.root)
      .setMessage(R.string.admin_settings_sub)
      .setPositiveButton(R.string.admin_settings_submit, null)
      .setNegativeButton(R.string.admin_settings_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .create()

    // https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        if (binding.inputPin.getInput().isEmpty()) {
          return@setOnClickListener
        }
        if (binding.inputPin.getInput() == adminPin) {
          routeDialogInterface.routeToResetPinDialog()
        } else {
          adminViewModel.errorMessage.set(fragment.resources.getString(R.string.admin_settings_incorrect))
        }
      }
    }
    return dialog
  }

  private fun getAdminSettingsViewModel(): AdminSettingsViewModel {
    return viewModelProvider.getForFragment(fragment, AdminSettingsViewModel::class.java)
  }
}

package org.oppia.android.app.profile

import android.app.Dialog
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.oppia.android.app.R
import org.oppia.android.app.databinding.AdminSettingsDialogBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ViewModelProvider
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

    binding.inputPin.setInput(adminViewModel.inputPin.get().toString())
    binding.inputPin.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(confirmPin: CharSequence?, start: Int, before: Int, count: Int) {
        confirmPin?.let {
          adminViewModel.inputPin.set(confirmPin.toString())
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

    // This logic prevents the dialog from being dismissed. https://stackoverflow.com/a/7636468.
    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        if (binding.inputPin.getInput().isEmpty()) {
          return@setOnClickListener
        }
        if (binding.inputPin.getInput() == adminPin) {
          routeDialogInterface.routeToResetPinDialog()
        } else {
          adminViewModel.errorMessage.set(
            fragment.resources.getString(
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

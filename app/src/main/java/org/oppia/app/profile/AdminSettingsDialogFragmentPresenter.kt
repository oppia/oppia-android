package org.oppia.app.profile

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.AdminSettingsDialogBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [AdminAuthActivity]. */
@FragmentScope
class AdminSettingsDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<AdminSettingsViewModel>,
  private val logger: Logger
) {
  private val adminViewModel by lazy {
    getAdminSettingsViewModel()
  }

  fun handleOnCreateDialog(): Dialog {
    val adminPin = fragment.arguments?.getString(KEY_ADMIN_SETTINGS_PIN)
    checkNotNull(adminPin) { "Admin Pin must not be null" }
    val binding: AdminSettingsDialogBinding = DataBindingUtil.inflate(fragment.requireActivity().layoutInflater, R.layout.admin_settings_dialog, null, false)
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
//    val cellularDataInterface: CellularDataInterface = parentFragment as StateFragment

    return AlertDialog.Builder(fragment.requireActivity() as Context)
      .setTitle(R.string.admin_settings_heading)
      .setView(binding.root)
      .setMessage(R.string.admin_settings_sub)
      .setPositiveButton(R.string.admin_settings_submit) { dialog, whichButton ->
        //        cellularDataInterface.enableAudioWhileOnCellular(checkBox.isChecked)
        logger.e("James", "Positive")
        if (binding.inputPin.getInput() == adminPin) {
          (fragment as DialogFragment).dismiss()
        } else {
          adminViewModel.errorMessage.set(fragment.resources.getString(R.string.admin_settings_incorrect))
        }
      }
      .setNegativeButton(R.string.admin_settings_cancel) { dialog, whichButton ->
        //        cellularDataInterface.disableAudioWhileOnCellular(checkBox.isChecked)
        logger.e("James", "Positive")
        (fragment as DialogFragment).dismiss()
      }
      .create()
  }

  private fun getAdminSettingsViewModel(): AdminSettingsViewModel {
    return viewModelProvider.getForFragment(fragment, AdminSettingsViewModel::class.java)
  }
}

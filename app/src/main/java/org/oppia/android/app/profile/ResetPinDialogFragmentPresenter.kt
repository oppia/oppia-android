package org.oppia.android.app.profile

import android.app.Dialog
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ResetPinDialogBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [ResetPinDialogFragment]. */
@FragmentScope
class ResetPinDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<ResetPinViewModel>
) {
  private val resetViewModel by lazy {
    getResetPinViewModel()
  }

  fun handleOnCreateDialog(
    routeDialogInterface: ProfileRouteDialogInterface,
    profileId: Int,
    name: String
  ): Dialog {
    val binding: ResetPinDialogBinding = DataBindingUtil.inflate(
      activity.layoutInflater,
      R.layout.reset_pin_dialog,
      /* parent= */ null,
      /* attachToParent= */ false
    )
    binding.apply {
      lifecycleOwner = fragment
      viewModel = resetViewModel
    }
    resetViewModel.name.set(name)

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.resetPinInputPinEditText.onTextChanged { confirmPin ->
      confirmPin?.let {
        // To avoid disappearing Error message due to orientation change.
        if (
          resetViewModel.errorMessage.get()?.isNotEmpty()!! && resetViewModel.inputPin.get() == it
        ) {
          resetViewModel.inputPin.set(it)
        } else {
          resetViewModel.inputPin.set(it)
          resetViewModel.errorMessage.set("")
        }
      }
    }

    val dialog = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setTitle(R.string.reset_pin_enter)
      .setView(binding.root)
      .setMessage("This PIN wil be $name's new PIN and will be required when signing in.")
      .setPositiveButton(R.string.admin_settings_submit, null)
      .setNegativeButton(R.string.admin_settings_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .create()

    binding.resetPinInputPinEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick()
      }
      false
    }

    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val input = binding.resetPinInputPinEditText.text.toString()
        if (input.isEmpty()) {
          return@setOnClickListener
        }
        if (input.length == 3) {
          profileManagementController
            .updatePin(ProfileId.newBuilder().setInternalId(profileId).build(), input).toLiveData()
            .observe(
              fragment,
              Observer {
                if (it.isSuccess()) {
                  routeDialogInterface.routeToSuccessDialog()
                }
              }
            )
        } else {
          resetViewModel.errorMessage.set(
            fragment.resources.getString(
              R.string.add_profile_error_pin_length
            )
          )
        }
      }
    }
    return dialog
  }

  private fun getResetPinViewModel(): ResetPinViewModel {
    return viewModelProvider.getForFragment(fragment, ResetPinViewModel::class.java)
  }
}

package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.databinding.PinPasswordActivityBinding
import org.oppia.app.home.HomeActivity
import org.oppia.app.model.ProfileId
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

private const val TAG_ADMIN_SETTINGS_DIALOG = "ADMIN_SETTNIGS_DIALOG"
private const val TAG_RESET_PIN_DIALOG = "RESET_PIN_DIALOG"


class PinPasswordActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<PinPasswordViewModel>
) {
  private val pinViewModel by lazy {
    getPinPasswordViewModel()
  }
  private var profileId = -1

  fun handleOnCreate() {
    val adminPin = activity.intent.getStringExtra(KEY_PIN_PASSWORD_ADMIN_PIN)
    profileId = activity.intent.getIntExtra(KEY_PIN_PASSWORD_PROFILE_ID, -1)
    val binding = DataBindingUtil.setContentView<PinPasswordActivityBinding>(activity, R.layout.pin_password_activity)
    pinViewModel.setProfileId(profileId)
    binding.apply {
      lifecycleOwner = activity
      viewModel = pinViewModel
    }

    binding.showPin.setOnClickListener {
      pinViewModel.showPassword.set(!pinViewModel.showPassword.get()!!)
    }

    binding.inputPin.addTextChangedListener(object: TextWatcher {
      override fun onTextChanged(pin: CharSequence?, start: Int, before: Int, count: Int) {
        pin?.let { inputtedPin ->
          pinViewModel.showError.set(false)
          if (inputtedPin.length == pinViewModel.correctPin.length) {
            if (inputtedPin.toString() == pinViewModel.correctPin) {
              profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(profileId).build())
                .observe(activity, Observer {
                if (it.isSuccess()) {
                  activity.startActivity(Intent(activity, HomeActivity::class.java))
                }
              })
            } else {
              binding.inputPin.setText("")
              pinViewModel.showError.set(true)
            }
          }
        }
      }
      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    binding.forgotPin.setOnClickListener {
      if (pinViewModel.isAdmin) {
        showAdminForgotPin()
      } else {
        val previousFrag = activity.supportFragmentManager.findFragmentByTag(TAG_ADMIN_SETTINGS_DIALOG)
        if (previousFrag != null) {
          activity.supportFragmentManager.beginTransaction().remove(previousFrag).commitNow()
        }
        val dialogFragment = AdminSettingsDialogFragment.newInstance(adminPin)
        dialogFragment.showNow(activity.supportFragmentManager, TAG_ADMIN_SETTINGS_DIALOG)
      }
    }
  }

  fun handleRouteToResetPinDialog() {
    (activity.supportFragmentManager.findFragmentByTag(TAG_ADMIN_SETTINGS_DIALOG) as DialogFragment).dismiss()
    val dialogFragment = ResetPinDialogFragment.newInstance(profileId, pinViewModel.name)
    dialogFragment.showNow(activity.supportFragmentManager, TAG_RESET_PIN_DIALOG)
  }

  fun handleRouteToSuccessDialog() {
    (activity.supportFragmentManager.findFragmentByTag(TAG_RESET_PIN_DIALOG) as DialogFragment).dismiss()
    showSuccessDialog()
  }

  private fun getPinPasswordViewModel(): PinPasswordViewModel {
    return viewModelProvider.getForActivity(activity, PinPasswordViewModel::class.java)
  }

  private fun showAdminForgotPin() {
    AlertDialog.Builder(activity as Context, R.style.AlertDialogTheme)
      .setTitle(R.string.pin_password_forgot_title)
      .setMessage(R.string.pin_password_forgot_message)
      .setNegativeButton(R.string.admin_settings_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.pin_password_play_store) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }

  private fun showSuccessDialog() {
    AlertDialog.Builder(activity as Context, R.style.AlertDialogTheme)
      .setMessage(R.string.pin_password_success)
      .setPositiveButton(R.string.pin_password_close) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }


}
package org.oppia.app.profile

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.databinding.PinPasswordActivityBinding
import org.oppia.app.home.HomeActivity
import org.oppia.app.model.ProfileId
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

private const val TAG_ADMIN_SETTINGS_DIALOG = "ADMIN_SETTNIGS_DIALOG"

class PinPasswordActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<PinPasswordViewModel>
) {
  private val pinViewModel by lazy {
    getPinPasswordViewModel()
  }

  fun handleOnCreate() {
    val name = activity.intent.getStringExtra(KEY_PROFILE_NAME)
    val correctPin = activity.intent.getStringExtra(KEY_CORRECT_PIN)
    val adminPin = activity.intent.getStringExtra(KEY_PIN_PASSWORD_ADMIN_PIN)
    val profileId = activity.intent.getIntExtra(KEY_PIN_PASSWORD_PROFILE_ID, -1)

    val binding = DataBindingUtil.setContentView<PinPasswordActivityBinding>(activity, R.layout.pin_password_activity)
    binding.helloText.text = "Hi, $name!\nPlease enter your PIN."
    binding.inputPin.itemCount = correctPin.length
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
          if (inputtedPin.length == correctPin.length) {
            if (inputtedPin.toString() == correctPin) {
              profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(profileId).build())
                .observe(activity, Observer {
                if (it.isSuccess()) {
                  activity.startActivity(Intent(activity, HomeActivity::class.java))
                } else {
                  //TODO Handle other error cases
                }
              })
            } else {
              pinViewModel.showError.set(true)
            }
          }
        }
      }
      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    binding.forgotPin.setOnClickListener {
      if (correctPin.length == 5) {

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

  private fun getPinPasswordViewModel(): PinPasswordViewModel {
    return viewModelProvider.getForActivity(activity, PinPasswordViewModel::class.java)
  }
}
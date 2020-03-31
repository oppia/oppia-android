package org.oppia.app.settings.profile

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.ProfileResetPinActivityBinding
import org.oppia.app.model.ProfileId
import org.oppia.app.profile.ProfileInputView
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileResetPinActivity]. */
@ActivityScope
class ProfileResetPinActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<ProfileResetPinViewModel>
) {
  private var inputtedPin = false
  private var inputtedConfirmPin = false

  private val resetViewModel: ProfileResetPinViewModel by lazy {
    getProfileResetPinViewModel()
  }

  fun handleOnCreate() {
    activity.title = activity.getString(R.string.profile_reset_pin_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding =
      DataBindingUtil.setContentView<ProfileResetPinActivityBinding>(activity, R.layout.profile_reset_pin_activity)
    val profileId = activity.intent.getIntExtra(KEY_PROFILE_RESET_PIN_PROFILE_ID, 0)
    val isAdmin = activity.intent.getBooleanExtra(KEY_PROFILE_RESET_PIN_IS_ADMIN, false)
    resetViewModel.isAdmin.set(isAdmin)

    binding.profileResetPinToolbar.setNavigationOnClickListener {
      (activity as ProfileRenameActivity).finish()
    }

    binding.apply {
      viewModel = resetViewModel
      lifecycleOwner = activity
    }

    binding.inputPin.post {
      addTextChangedListener(binding.inputPin) { pin ->
        pin?.let {
          resetViewModel.inputPin.set(it.toString())
          resetViewModel.pinErrorMsg.set("")
          inputtedPin = pin.isNotEmpty()
          setValidPin()
        }
      }
    }
    binding.inputConfirmPin.post {
      addTextChangedListener(binding.inputConfirmPin) { confirmPin ->
        confirmPin?.let {
          resetViewModel.inputConfirmPin.set(it.toString())
          resetViewModel.confirmErrorMsg.set("")
          inputtedConfirmPin = confirmPin.isNotEmpty()
          setValidPin()
        }
      }
    }

    binding.inputPin.setInput(resetViewModel.inputPin.get().toString())
    binding.inputConfirmPin.setInput(resetViewModel.inputConfirmPin.get().toString())

    binding.profileResetSaveButton.setOnClickListener {
      val pin = binding.inputPin.getInput()
      val confirmPin = binding.inputConfirmPin.getInput()
      var failed = false
      if (isAdmin) {
        if (pin.length < 5) {
          resetViewModel.pinErrorMsg.set(activity.resources.getString(R.string.profile_reset_pin_error_admin_pin_length))
          failed = true
        }
      } else {
        if (pin.length < 3) {
          resetViewModel.pinErrorMsg.set(activity.resources.getString(R.string.profile_reset_pin_error_user_pin_length))
          failed = true
        }
      }
      if (pin != confirmPin) {
        resetViewModel.confirmErrorMsg.set(activity.resources.getString(R.string.add_profile_error_pin_confirm_wrong))
        failed = true
      }
      if (failed) {
        return@setOnClickListener
      }
      profileManagementController.updatePin(ProfileId.newBuilder().setInternalId(profileId).build(), pin)
        .observe(activity, Observer {
          if (it.isSuccess()) {
            val intent = ProfileEditActivity.createProfileEditActivity(activity, profileId)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
          }
        })
    }
  }

  private fun setValidPin() {
    if (inputtedPin && inputtedConfirmPin) {
      resetViewModel.isButtonActive.set(true)
    } else {
      resetViewModel.isButtonActive.set(false)
    }
  }

  private fun addTextChangedListener(profileInputView: ProfileInputView, onTextChanged: (CharSequence?) -> Unit) {
    profileInputView.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        onTextChanged(p0)
      }

      override fun afterTextChanged(p0: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    })
  }

  private fun getProfileResetPinViewModel(): ProfileResetPinViewModel {
    return viewModelProvider.getForActivity(activity, ProfileResetPinViewModel::class.java)
  }
}

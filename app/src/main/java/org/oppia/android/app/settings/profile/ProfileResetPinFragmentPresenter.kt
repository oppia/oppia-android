package org.oppia.android.app.settings.profile

import android.content.Intent
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.databinding.ProfileResetPinFragmentBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [ProfileResetPinFragment]. */
class ProfileResetPinFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileResetPinViewModel: ProfileResetPinViewModel,
  private val profileManagementController: ProfileManagementController,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding: ProfileResetPinFragmentBinding
  private var inputtedPin = false
  private var inputtedConfirmPin = false

  /** Handles onCreateView() method of the [ProfileResetPinFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: Int,
    isAdmin: Boolean
  ): View? {
    binding = ProfileResetPinFragmentBinding.inflate(inflater, container, false)

    profileResetPinViewModel.isAdmin.set(isAdmin)
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = profileResetPinViewModel
    }

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.profileResetInputPinEditText.onTextChanged { pin ->
      pin?.let {
        if (
          profileResetPinViewModel.pinErrorMsg.get()?.isNotEmpty()!! &&
          profileResetPinViewModel.inputPin.get() == it
        ) {
          profileResetPinViewModel.inputPin.set(it)
          inputtedPin = pin.isNotEmpty()
        } else {
          profileResetPinViewModel.inputPin.set(it)
          profileResetPinViewModel.pinErrorMsg.set("")
          inputtedPin = pin.isNotEmpty()
          setValidPin()
        }
      }
    }

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.profileResetInputConfirmPinEditText.onTextChanged { confirmPin ->
      confirmPin?.let {
        if (
          profileResetPinViewModel.confirmErrorMsg.get()?.isNotEmpty()!! &&
          profileResetPinViewModel.inputConfirmPin.get() == it
        ) {
          profileResetPinViewModel.inputConfirmPin.set(it)
          inputtedConfirmPin = confirmPin.isNotEmpty()
        } else {
          profileResetPinViewModel.inputConfirmPin.set(it)
          profileResetPinViewModel.confirmErrorMsg.set("")
          inputtedConfirmPin = confirmPin.isNotEmpty()
          setValidPin()
        }
      }
    }

    binding.profileResetInputConfirmPinEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        binding.profileResetSaveButton.callOnClick()
      }
      false
    }

    binding.profileResetSaveButton.setOnClickListener {
      val pin = binding.profileResetInputPinEditText.text.toString()
      val confirmPin = binding.profileResetInputConfirmPinEditText.text.toString()
      var failed = false
      if (isAdmin) {
        if (pin.length < 5) {
          profileResetPinViewModel.pinErrorMsg.set(
            resourceHandler.getStringInLocale(
              R.string.profile_reset_pin_error_admin_pin_length
            )
          )
          failed = true
        }
      } else {
        if (pin.length < 3) {
          profileResetPinViewModel.pinErrorMsg.set(
            resourceHandler.getStringInLocale(
              R.string.profile_reset_pin_error_user_pin_length
            )
          )
          failed = true
        }
      }
      if (pin != confirmPin) {
        profileResetPinViewModel.confirmErrorMsg.set(
          resourceHandler.getStringInLocale(
            R.string.add_profile_error_pin_confirm_wrong
          )
        )
        failed = true
      }
      if (failed) {
        return@setOnClickListener
      }
      profileManagementController
        .updatePin(ProfileId.newBuilder().setLoggedInInternalProfileId(profileId).build(), pin)
        .toLiveData()
        .observe(
          activity,
          Observer {
            if (it is AsyncResult.Success) {
              val intent = ProfileEditActivity.createProfileEditActivity(activity, profileId)
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
              activity.startActivity(intent)
            }
          }
        )
    }
    return binding.root
  }

  private fun setValidPin() {
    if (inputtedPin && inputtedConfirmPin) {
      profileResetPinViewModel.isButtonActive.set(true)
    } else {
      profileResetPinViewModel.isButtonActive.set(false)
    }
  }
}

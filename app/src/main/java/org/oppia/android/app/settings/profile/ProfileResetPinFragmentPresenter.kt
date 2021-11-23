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
import org.oppia.android.databinding.ProfileResetPinFragmentBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged

/** The presenter for [ProfileResetPinFragment]. */
class ProfileResetPinFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var viewModel: ProfileResetPinViewModel
  private lateinit var binding: ProfileResetPinFragmentBinding
  private var inputtedPin = false
  private var inputtedConfirmPin = false

  /** Handles onCreateView() method of the [ProfileResetPinFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileManagementController: ProfileManagementController,
    profileId: Int,
    isAdmin: Boolean
  ): View? {
    viewModel = getProfileResetPinViewModel()

    binding = ProfileResetPinFragmentBinding.inflate(inflater, container, false)

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    binding.profileResetInputPinEditText.onTextChanged { pin ->
      pin?.let {
        if (
          viewModel.pinErrorMsg.get()?.isNotEmpty()!! &&
          viewModel.inputPin.get() == it
        ) {
          viewModel.inputPin.set(it)
          inputtedPin = pin.isNotEmpty()
        } else {
          viewModel.inputPin.set(it)
          viewModel.pinErrorMsg.set("")
          inputtedPin = pin.isNotEmpty()
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
          viewModel.pinErrorMsg.set(
            resourceHandler.getStringInLocale(
              R.string.profile_reset_pin_error_admin_pin_length
            )
          )
          failed = true
        }
      } else {
        if (pin.length < 3) {
          viewModel.pinErrorMsg.set(
            resourceHandler.getStringInLocale(
              R.string.profile_reset_pin_error_user_pin_length
            )
          )
          failed = true
        }
      }
      if (pin != confirmPin) {
        viewModel.confirmErrorMsg.set(
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
        .updatePin(ProfileId.newBuilder().setInternalId(profileId).build(), pin).toLiveData()
        .observe(
          activity,
          Observer {
            if (it.isSuccess()) {
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
      viewModel.isButtonActive.set(true)
    } else {
      viewModel.isButtonActive.set(false)
    }
  }

  private fun getProfileResetPinViewModel(): ProfileResetPinViewModel {
    return ProfileResetPinViewModel()
  }
}
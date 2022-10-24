package org.oppia.android.app.profile

import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.AdminPinActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [AdminPinActivity]. */
@ActivityScope
class AdminPinActivityPresenter @Inject constructor(
  private val context: Context,
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<AdminPinViewModel>,
  private val resourceHandler: AppLanguageResourceHandler
) {

  private var inputtedPin = false
  private var inputtedConfirmPin = false

  private val adminViewModel by lazy {
    getAdminPinViewModel()
  }

  /** Binds ViewModel and sets up text and button listeners. */
  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
    activity.supportActionBar?.setHomeActionContentDescription(R.string.admin_auth_activity_close_button_text)

    val binding =
      DataBindingUtil.setContentView<AdminPinActivityBinding>(activity, R.layout.admin_pin_activity)

    binding.apply {
      lifecycleOwner = activity
      viewModel = adminViewModel
    }

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.adminPinInputPinEditText.onTextChanged { pin ->
      pin?.let {
        if (adminViewModel.pinErrorMsg.get()?.isNotEmpty()!! &&
          adminViewModel.savedPin.get() == it
        ) {
          adminViewModel.savedPin.set(it)
          inputtedPin = pin.isNotEmpty()
        } else {
          adminViewModel.pinErrorMsg.set("")
          adminViewModel.savedPin.set(it)
          inputtedPin = pin.isNotEmpty()
          setValidPin()
        }
      }
    }

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.adminPinInputConfirmPinEditText.onTextChanged { confirmPin ->
      confirmPin?.let {
        if (adminViewModel.confirmPinErrorMsg.get()?.isNotEmpty()!! &&
          adminViewModel.savedConfirmPin.get() == it
        ) {
          adminViewModel.savedConfirmPin.set(it)
          inputtedConfirmPin = confirmPin.isNotEmpty()
        } else {
          adminViewModel.confirmPinErrorMsg.set("")
          adminViewModel.savedConfirmPin.set(it)
          inputtedConfirmPin = confirmPin.isNotEmpty()
          setValidPin()
        }
      }
    }

    binding.submitButton.setOnClickListener {
      val inputPin = binding.adminPinInputPinEditText.text.toString()
      val confirmPin = binding.adminPinInputConfirmPinEditText.text.toString()
      var failed = false
      if (inputPin.length < 5) {
        adminViewModel.pinErrorMsg.set(
          resourceHandler.getStringInLocale(
            R.string.admin_pin_activity_admin_pin_length_error_text
          )
        )
        failed = true
      }
      if (inputPin != confirmPin) {
        adminViewModel.confirmPinErrorMsg.set(
          resourceHandler.getStringInLocale(
            R.string.admin_pin_activity_wrong_confirm_admin_pin_error_text
          )
        )
        failed = true
      }
      if (failed) {
        return@setOnClickListener
      }
      val profileId =
        ProfileId.newBuilder()
          .setInternalId(activity.intent.getIntExtra(ADMIN_PIN_PROFILE_ID_EXTRA_KEY, -1))
          .build()

      profileManagementController.updatePin(profileId, inputPin).toLiveData().observe(
        activity,
        Observer {
          if (it is AsyncResult.Success) {
            when (activity.intent.getIntExtra(ADMIN_PIN_ENUM_EXTRA_KEY, 0)) {
              AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value -> {
                activity.startActivity(
                  AdministratorControlsActivity.createAdministratorControlsActivityIntent(
                    context, activity.intent.getIntExtra(ADMIN_PIN_PROFILE_ID_EXTRA_KEY, -1)
                  )
                )
                activity.finish()
              }
              AdminAuthEnum.PROFILE_ADD_PROFILE.value -> {
                activity.startActivity(
                  AddProfileActivity.createAddProfileActivityIntent(
                    context,
                    activity.intent.getIntExtra(
                      ADMIN_PIN_COLOR_RGB_EXTRA_KEY, -10710042
                    )
                  )
                )
                activity.finish()
              }
            }
          }
        }
      )
    }

    binding.adminPinInputConfirmPinEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        binding.submitButton.callOnClick()
      }
      false
    }
    binding.adminPinInputPinEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        binding.submitButton.callOnClick()
      }
      false
    }
  }

  private fun setValidPin() {
    if (inputtedPin && inputtedConfirmPin) {
      getAdminPinViewModel().isButtonActive.set(true)
    } else {
      getAdminPinViewModel().isButtonActive.set(false)
    }
  }

  private fun getAdminPinViewModel(): AdminPinViewModel {
    return viewModelProvider.getForActivity(activity, AdminPinViewModel::class.java)
  }
}

package org.oppia.android.app.profile

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.AdminAuthActivityBinding
import javax.inject.Inject

const val KEY_ADMIN_AUTH_INPUT_ERROR_MESSAGE = "ADMIN_AUTH_INPUT_ERROR_MESSAGE"
const val KEY_ADMIN_AUTH_INPUT_PASSWORD = "ADMIN_AUTH_INPUT_PASSWORD"

/** The presenter for [AdminAuthActivity]. */
@ActivityScope
class AdminAuthActivityPresenter @Inject constructor(
  private val context: Context,
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<AdminAuthViewModel>
) {
  private lateinit var binding: AdminAuthActivityBinding
  private val authViewModel by lazy {
    getAdminAuthViewModel()
  }

  /** Binds ViewModel and sets up text and button listeners. */
  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView<AdminAuthActivityBinding>(
      activity,
      R.layout.admin_auth_activity
    )
    binding.adminAuthToolbar.setNavigationOnClickListener {
      (activity as AdminAuthActivity).finish()
    }
    val adminPin = activity.intent.getStringExtra(ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY)
    binding.apply {
      lifecycleOwner = activity
      viewModel = authViewModel
    }

    setTitleAndSubTitle(binding)

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.adminAuthInputPinEditText.onTextChanged { confirmPin ->
      confirmPin?.let {
        authViewModel.errorMessage.set("")
      }
    }

    binding.adminAuthInputPinEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        binding.adminAuthSubmitButton.callOnClick()
      }
      false
    }

    binding.adminAuthSubmitButton.setOnClickListener {
      val inputPin = binding.adminAuthInputPinEditText.text.toString()
      if (inputPin.isEmpty()) {
        return@setOnClickListener
      }
      if (inputPin == adminPin) {
        when (activity.intent.getIntExtra(ADMIN_AUTH_ENUM_EXTRA_KEY, 0)) {
          AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value -> {
            activity.startActivity(
              AdministratorControlsActivity.createAdministratorControlsActivityIntent(
                context, activity.intent.getIntExtra(ADMIN_AUTH_PROFILE_ID_EXTRA_KEY, -1)
              )
            )
            activity.finish()
          }
          AdminAuthEnum.PROFILE_ADD_PROFILE.value -> {
            activity.startActivity(
              AddProfileActivity.createAddProfileActivityIntent(
                context, activity.intent.getIntExtra(ADMIN_AUTH_COLOR_RGB_EXTRA_KEY, -10710042)
              )
            )
            activity.finish()
          }
        }
      } else {
        authViewModel.errorMessage.set(activity.resources.getString(R.string.admin_auth_incorrect))
      }
    }
  }

  private fun setTitleAndSubTitle(binding: AdminAuthActivityBinding?) {
    when (activity.intent.getIntExtra(ADMIN_AUTH_ENUM_EXTRA_KEY, 0)) {
      AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value -> {
        binding?.adminAuthToolbar?.title =
          context.resources.getString(R.string.administrator_controls)
        binding?.adminAuthHeadingTextview?.text =
          context.resources.getString(R.string.admin_auth_heading)
        binding?.adminAuthSubText?.text =
          context.resources.getString(R.string.admin_auth_admin_controls_sub)
      }
      AdminAuthEnum.PROFILE_ADD_PROFILE.value -> {
        binding?.adminAuthToolbar?.title = context.resources.getString(R.string.add_profile_title)
        binding?.adminAuthHeadingTextview?.text =
          context.resources.getString(R.string.admin_auth_heading)
        binding?.adminAuthSubText?.text = context.resources.getString(R.string.admin_auth_sub)
      }
    }
  }

  fun handleOnSavedInstanceState(bundle: Bundle) {
    bundle.putString(KEY_ADMIN_AUTH_INPUT_ERROR_MESSAGE, authViewModel.errorMessage.get())
    bundle.putString(
      KEY_ADMIN_AUTH_INPUT_PASSWORD,
      binding.adminAuthInputPinEditText.text.toString()
    )
  }

  fun handleOnRestoreInstanceState(bundle: Bundle) {
    val errorMessage = bundle.getString(KEY_ADMIN_AUTH_INPUT_ERROR_MESSAGE)
    val password = bundle.getString(KEY_ADMIN_AUTH_INPUT_PASSWORD)
    if (!password.isNullOrEmpty()) {
      binding.adminAuthInputPinEditText.setText(password)
      binding.adminAuthInputPinEditText.setSelection(password.length)
    }
    if (errorMessage != null && errorMessage.isNotEmpty()) {
      authViewModel.errorMessage.set(errorMessage)
    }
  }

  private fun getAdminAuthViewModel(): AdminAuthViewModel {
    return viewModelProvider.getForActivity(activity, AdminAuthViewModel::class.java)
  }
}

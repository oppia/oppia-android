package org.oppia.app.profile

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.app.databinding.AdminAuthActivityBinding
import org.oppia.app.viewmodel.ViewModelProvider
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
    binding = DataBindingUtil.setContentView<AdminAuthActivityBinding>(activity, R.layout.admin_auth_activity)
    binding.adminAuthToolbar.setNavigationOnClickListener {
      (activity as AdminAuthActivity).finish()
    }
    val adminPin = activity.intent.getStringExtra(KEY_ADMIN_AUTH_ADMIN_PIN)
    binding.apply {
      lifecycleOwner = activity
      viewModel = authViewModel
    }

    setTitleAndSubTitle(binding)

    binding.adminAuthInputPin.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(confirmPin: CharSequence?, start: Int, before: Int, count: Int) {
        confirmPin?.let {
          authViewModel.errorMessage.set("")
        }
      }

      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    binding.adminAuthSubmitButton.setOnClickListener {
      val inputPin = binding.adminAuthInputPin.getInput()
      if (inputPin.isEmpty()) {
        return@setOnClickListener
      }
      if (inputPin == adminPin) {
        when (activity.intent.getIntExtra(KEY_ADMIN_AUTH_ENUM, 0)) {
          AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value -> {
            activity.startActivity(
              AdministratorControlsActivity.createAdministratorControlsActivityIntent(
                context, activity.intent.getIntExtra(KEY_ADMIN_AUTH_PROFILE_ID, -1)
              )
            )
          }
          AdminAuthEnum.PROFILE_ADD_PROFILE.value -> {
            activity.startActivity(
              AddProfileActivity.createAddProfileActivityIntent(
                context, activity.intent.getIntExtra(KEY_ADMIN_AUTH_COLOR_RGB, -10710042)
              )
            )
          }
        }
      } else {
        authViewModel.errorMessage.set(activity.resources.getString(R.string.admin_auth_incorrect))
      }
    }
  }

  private fun setTitleAndSubTitle(binding: AdminAuthActivityBinding?) {
    when (activity.intent.getIntExtra(KEY_ADMIN_AUTH_ENUM, 0)) {
      AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value -> {
        binding?.adminAuthToolbar?.title = context.resources.getString(R.string.administrator_controls)
        binding?.adminAuthHeadingTextview?.text = context.resources.getString(R.string.admin_auth_heading)
        binding?.adminAuthSubText?.text = context.resources.getString(R.string.admin_auth_admin_controls_sub)
      }
      AdminAuthEnum.PROFILE_ADD_PROFILE.value -> {
        binding?.adminAuthToolbar?.title = context.resources.getString(R.string.add_profile_title)
        binding?.adminAuthHeadingTextview?.text = context.resources.getString(R.string.admin_auth_heading)
        binding?.adminAuthSubText?.text = context.resources.getString(R.string.admin_auth_sub)
      }
    }
  }

  fun   handleOnSavedInstanceState(bundle: Bundle) {
    bundle.putString(KEY_ADMIN_AUTH_INPUT_ERROR_MESSAGE, authViewModel.errorMessage.get())
    bundle.putString(KEY_ADMIN_AUTH_INPUT_PASSWORD, binding.adminAuthInputPin.getInput())
  }

  fun handleOnRestoreInstanceState(bundle: Bundle) {
    val errorMessage = bundle.getString(KEY_ADMIN_AUTH_INPUT_ERROR_MESSAGE)
    val password = bundle.getString(KEY_ADMIN_AUTH_INPUT_PASSWORD)
    if (!password.isNullOrEmpty()) {
      binding.adminAuthInputPin.setInput(password)
      binding.adminAuthInputPin.setSelection(password.length)
    }
    if (errorMessage != null && errorMessage.isNotEmpty()) {
      authViewModel.errorMessage.set(errorMessage)
    }
  }

  private fun getAdminAuthViewModel(): AdminAuthViewModel {
    return viewModelProvider.getForActivity(activity, AdminAuthViewModel::class.java)
  }
}

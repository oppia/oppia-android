package org.oppia.android.app.profile

import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.databinding.AdminAuthActivityBinding
import javax.inject.Inject
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.model.AddProfileActivityParams
import org.oppia.android.app.model.AdministratorControlsActivityParams
import org.oppia.android.app.model.DestinationScreen

/** The presenter for ``AdminAuthActivity``. */
@ActivityScope
class AdminAuthActivityPresenter @Inject constructor(
  private val context: Context,
  private val activity: AppCompatActivity,
  private val authViewModel: AdminAuthViewModel,
  private val resourceHandler: AppLanguageResourceHandler,
  private val activityRouter: ActivityRouter
) {
  private lateinit var binding: AdminAuthActivityBinding

  /** Binds ViewModel and sets up text and button listeners. */
  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView<AdminAuthActivityBinding>(
      activity,
      R.layout.admin_auth_activity
    )
    binding.adminAuthToolbar.setNavigationOnClickListener { activity.finish() }
    val adminPin = checkNotNull(activity.intent.getStringExtra(ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY)) {
      "Expected $ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY to be in intent extras."
    }
    binding.apply {
      lifecycleOwner = activity
      viewModel = authViewModel
    }

    setTitleAndSubTitle(binding)

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.adminAuthInputPinEditText.onTextChanged { pin ->
      pin?.let {
        if (
          authViewModel.errorMessage.get()?.isNotEmpty()!! &&
          authViewModel.inputPin.get() == it
        ) {
          authViewModel.inputPin.set(it)
        } else {
          authViewModel.inputPin.set(it)
          authViewModel.errorMessage.set("")
        }
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
            activityRouter.routeToScreen(
              DestinationScreen.newBuilder().apply {
                administratorControlsActivityParams =
                  AdministratorControlsActivityParams.newBuilder().apply {
                    this.internalProfileId =
                      activity.intent.getIntExtra(ADMIN_AUTH_PROFILE_ID_EXTRA_KEY, -1)
                  }.build()
              }.build()
            )
            activity.finish()
          }
          AdminAuthEnum.PROFILE_ADD_PROFILE.value -> {
            activityRouter.routeToScreen(
              DestinationScreen.newBuilder().apply {
                addProfileActivityParams = AddProfileActivityParams.newBuilder().apply {
                  this.colorRgb =
                    activity.intent.getIntExtra(ADMIN_AUTH_COLOR_RGB_EXTRA_KEY, -10710042)
                }.build()
              }.build()
            )
            activity.finish()
          }
        }
      } else if (inputPin.length == adminPin.length) {
        authViewModel.errorMessage.set(
          resourceHandler.getStringInLocale(R.string.admin_auth_incorrect)
        )
      }
    }
  }

  private fun setTitleAndSubTitle(binding: AdminAuthActivityBinding?) {
    when (activity.intent.getIntExtra(ADMIN_AUTH_ENUM_EXTRA_KEY, 0)) {
      AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value -> {
        activity.title =
          resourceHandler.getStringInLocale(R.string.admin_auth_activity_access_controls_title)
        binding?.adminAuthToolbar?.title =
          resourceHandler.getStringInLocale(R.string.administrator_controls)
        binding?.adminAuthHeadingTextview?.text =
          resourceHandler.getStringInLocale(R.string.admin_auth_heading)
        binding?.adminAuthSubText?.text =
          resourceHandler.getStringInLocale(R.string.admin_auth_admin_controls_sub)
      }
      AdminAuthEnum.PROFILE_ADD_PROFILE.value -> {
        activity.title =
          resourceHandler.getStringInLocale(R.string.admin_auth_activity_add_profiles_title)
        binding?.adminAuthToolbar?.title =
          resourceHandler.getStringInLocale(R.string.add_profile_title)
        binding?.adminAuthHeadingTextview?.text =
          resourceHandler.getStringInLocale(R.string.admin_auth_heading)
        binding?.adminAuthSubText?.text =
          resourceHandler.getStringInLocale(R.string.admin_auth_sub)
      }
    }
  }

  companion object {
    const val ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY = "AdminAuthActivity.admin_auth_admin_pin"
    const val ADMIN_AUTH_COLOR_RGB_EXTRA_KEY = "AdminAuthActivity.admin_auth_color_rgb"
    const val ADMIN_AUTH_ENUM_EXTRA_KEY = "AdminAuthActivity.admin_auth_enum"
    const val ADMIN_AUTH_PROFILE_ID_EXTRA_KEY = "AdminAuthActivity.admin_auth_profile_id"
  }
}

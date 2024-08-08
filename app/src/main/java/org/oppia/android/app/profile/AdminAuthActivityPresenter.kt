package org.oppia.android.app.profile

import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.model.AdminAuthActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.profile.AdminAuthActivity.Companion.ADMIN_AUTH_ACTIVITY_PARAMS_KEY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.databinding.AdminAuthActivityBinding
import org.oppia.android.util.extensions.getProtoExtra
import javax.inject.Inject

/** The presenter for [AdminAuthActivity]. */
@ActivityScope
class AdminAuthActivityPresenter @Inject constructor(
  private val context: Context,
  private val activity: AppCompatActivity,
  private val authViewModel: AdminAuthViewModel,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding: AdminAuthActivityBinding
  private val args by lazy {
    activity.intent.getProtoExtra(
      ADMIN_AUTH_ACTIVITY_PARAMS_KEY,
      AdminAuthActivityParams.getDefaultInstance()
    )
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
    val adminPin = checkNotNull(args?.adminPin) {
      "Expected AdminAuthActivity.admin_auth_admin_pin to be in intent extras."
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
        when (args?.adminPinEnum ?: 0) {
          AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value -> {
            val internalId = args?.internalProfileId ?: -1
            val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalId).build()
            activity.startActivity(
              AdministratorControlsActivity.createAdministratorControlsActivityIntent(
                context, profileId
              )
            )

            activity.finish()
          }
          AdminAuthEnum.PROFILE_ADD_PROFILE.value -> {
            activity.startActivity(
              AddProfileActivity.createAddProfileActivityIntent(
                context, args?.colorRgb ?: -10710042
              )
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
    when (args?.adminPinEnum ?: 0) {
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
}

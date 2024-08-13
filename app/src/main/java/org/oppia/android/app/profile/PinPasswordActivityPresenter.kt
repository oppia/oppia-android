package org.oppia.android.app.profile

import android.text.method.PasswordTransformationMethod
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.PinPasswordActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.profile.PinPasswordActivity.Companion.PIN_PASSWORD_ACTIVITY_PARAMS_KEY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.app.utility.lifecycle.LifecycleSafeTimerFactory
import org.oppia.android.databinding.PinPasswordActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject
import kotlin.system.exitProcess

private const val TAG_ADMIN_SETTINGS_DIALOG = "ADMIN_SETTINGS_DIALOG"
private const val TAG_RESET_PIN_DIALOG = "RESET_PIN_DIALOG"

/** The presenter for [PinPasswordActivity]. */
class PinPasswordActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  private val pinViewModel: PinPasswordViewModel,
  private val resourceHandler: AppLanguageResourceHandler,
  private val accessibilityService: AccessibilityService,
  @EnableMultipleClassrooms private val enableMultipleClassrooms: PlatformParameterValue<Boolean>,
) {
  private var internalProfileId =
    ProfileId.newBuilder().setLoggedOut(true).build().loggedInInternalProfileId
  private var profileId = ProfileId.getDefaultInstance()
  private lateinit var alertDialog: AlertDialog
  private var confirmedDeletion = false

  fun handleOnCreate() {
    val args = activity.intent.getProtoExtra(
      PIN_PASSWORD_ACTIVITY_PARAMS_KEY,
      PinPasswordActivityParams.getDefaultInstance()
    )

    val adminPin = args?.adminPin
    internalProfileId = args?.internalProfileId ?: ProfileId.newBuilder().setLoggedOut(true)
      .build().loggedInInternalProfileId
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()

    val binding = DataBindingUtil.setContentView<PinPasswordActivityBinding>(
      activity,
      R.layout.pin_password_activity
    )
    pinViewModel.setProfileId(internalProfileId)
    binding.apply {
      lifecycleOwner = activity
      viewModel = pinViewModel
    }

    binding.pinPasswordToolbar.setNavigationOnClickListener {
      (activity as PinPasswordActivity).finish()
    }

    binding.showPin.setOnClickListener {
      pinViewModel.showPassword.set(!pinViewModel.showPassword.get()!!)
      if (!pinViewModel.showPassword.get()!!) {
        binding.pinPasswordInputPinEditText.transformationMethod = PasswordTransformationMethod()
        binding.pinPasswordInputPinEditText.setSelection(
          binding.pinPasswordInputPinEditText.text.toString().length
        )
      } else {
        binding.pinPasswordInputPinEditText.transformationMethod = null
        binding.pinPasswordInputPinEditText.setSelection(
          binding.pinPasswordInputPinEditText.text.toString().length
        )
      }
    }

    // If the screen reader is off, the EditText will receive focus.
    // If the screen reader is on, the EditText won't receive focus.
    // This is needed because requesting focus on the EditText when the screen reader is on gives TalkBack priority over other views in the screen, ignoring view hierachy.
    if (!accessibilityService.isScreenReaderEnabled())
      binding.pinPasswordInputPinEditText.requestFocus()

    // [onTextChanged] is a extension function defined at [TextInputEditTextHelper]
    binding.pinPasswordInputPinEditText.onTextChanged { pin ->
      pin?.let { inputtedPin ->
        if (inputtedPin.isNotEmpty()) {
          pinViewModel.errorMessage.set("")
        }
        if (inputtedPin.length == pinViewModel.correctPin.get()!!.length &&
          inputtedPin.isNotEmpty() && pinViewModel.correctPin.get()!!
            .isNotEmpty()
        ) {
          if (inputtedPin == pinViewModel.correctPin.get()) {
            profileManagementController
              .loginToProfile(profileId).toLiveData().observe(
                activity,
                {
                  if (it is AsyncResult.Success) {
                    activity.startActivity(
                      if (enableMultipleClassrooms.value)
                        ClassroomListActivity.createClassroomListActivity(activity, profileId)
                      else
                        HomeActivity.createHomeActivity(activity, profileId)
                    )
                  }
                }
              )
          } else {
            pinViewModel.errorMessage.set(
              resourceHandler.getStringInLocale(R.string.pin_password_incorrect_pin)
            )
            binding.pinPasswordInputPinEditText.startAnimation(
              AnimationUtils.loadAnimation(
                activity,
                R.anim.shake
              )
            )
            lifecycleSafeTimerFactory.createTimer(1000).observe(
              activity,
              {
                binding.pinPasswordInputPinEditText.setText("")
              }
            )
          }
        }
      }
    }

    binding.forgotPin.setOnClickListener {
      if (pinViewModel.isAdmin.get()!!) {
        showAdminForgotPin()
      } else {
        val previousFrag =
          activity.supportFragmentManager.findFragmentByTag(TAG_ADMIN_SETTINGS_DIALOG)
        if (previousFrag != null) {
          activity.supportFragmentManager.beginTransaction().remove(previousFrag).commitNow()
        }
        val dialogFragment = AdminSettingsDialogFragment
          .newInstance(adminPin!!)
        dialogFragment.showNow(activity.supportFragmentManager, TAG_ADMIN_SETTINGS_DIALOG)
      }
    }

    if (pinViewModel.showAdminPinForgotPasswordPopUp.get()!!) {
      showAdminForgotPin()
    }
  }

  fun handleRouteToResetPinDialog() {
    (
      activity
        .supportFragmentManager
        .findFragmentByTag(
          TAG_ADMIN_SETTINGS_DIALOG
        ) as DialogFragment
      ).dismiss()
    val dialogFragment = ResetPinDialogFragment.newInstance(
      internalProfileId,
      pinViewModel.name.get()!!
    )
    dialogFragment.showNow(activity.supportFragmentManager, TAG_RESET_PIN_DIALOG)
  }

  fun handleRouteToSuccessDialog() {
    (
      activity
        .supportFragmentManager
        .findFragmentByTag(
          TAG_RESET_PIN_DIALOG
        ) as DialogFragment
      ).dismiss()
    showSuccessDialog()
  }

  private fun showAdminForgotPin() {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    pinViewModel.showAdminPinForgotPasswordPopUp.set(true)
    val resetDataButtonText =
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.admin_forgot_pin_reset_app_data_button_text, appName
      )
    alertDialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setTitle(R.string.pin_password_forgot_title)
      .setMessage(
        resourceHandler.getStringInLocaleWithWrapping(R.string.admin_forgot_pin_message, appName)
      )
      .setNegativeButton(R.string.admin_settings_cancel) { dialog, _ ->
        pinViewModel.showAdminPinForgotPasswordPopUp.set(false)
        dialog.dismiss()
      }
      .setPositiveButton(resetDataButtonText) { dialog, _ ->
        // Show a confirmation dialog since this is a permanent action.
        dialog.dismiss()
        showConfirmAppResetDialog()
      }.create()
    alertDialog.setCanceledOnTouchOutside(false)
    alertDialog.show()
  }

  private fun showConfirmAppResetDialog() {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    alertDialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setTitle(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.admin_confirm_app_wipe_title, appName
        )
      )
      .setMessage(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.admin_confirm_app_wipe_message, appName
        )
      )
      .setNegativeButton(R.string.admin_confirm_app_wipe_negative_button_text) { dialog, _ ->
        pinViewModel.showAdminPinForgotPasswordPopUp.set(false)
        dialog.dismiss()
      }
      .setPositiveButton(R.string.admin_confirm_app_wipe_positive_button_text) { _, _ ->
        profileManagementController.deleteAllProfiles().toLiveData().observe(activity) {
          // Regardless of the result of the operation, always restart the app.
          confirmedDeletion = true
          activity.finishAffinity()
        }
      }.create()
    alertDialog.setCanceledOnTouchOutside(false)
    alertDialog.show()
  }

  fun handleOnDestroy() {
    if (::alertDialog.isInitialized && alertDialog.isShowing) {
      alertDialog.dismiss()
    }

    if (confirmedDeletion) {
      confirmedDeletion = false

      // End the process forcibly since the app is not designed to recover from major on-disk state
      // changes that happen from underneath it (like deleting all profiles).
      exitProcess(0)
    }
  }

  private fun showSuccessDialog() {
    AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setMessage(R.string.pin_password_success)
      .setPositiveButton(R.string.pin_password_close) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }
}

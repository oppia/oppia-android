package org.oppia.android.app.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.method.PasswordTransformationMethod
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import org.oppia.android.R
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.LifecycleSafeTimerFactory
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.PinPasswordActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

private const val TAG_ADMIN_SETTINGS_DIALOG = "ADMIN_SETTINGS_DIALOG"
private const val TAG_RESET_PIN_DIALOG = "RESET_PIN_DIALOG"

/** The presenter for [PinPasswordActivity]. */
class PinPasswordActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  private val viewModelProvider: ViewModelProvider<PinPasswordViewModel>,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private val pinViewModel by lazy {
    getPinPasswordViewModel()
  }
  private var profileId = -1
  private lateinit var alertDialog: AlertDialog

  fun handleOnCreate() {
    val adminPin = activity.intent.getStringExtra(PIN_PASSWORD_ADMIN_PIN_EXTRA_KEY)
    profileId = activity.intent.getIntExtra(PIN_PASSWORD_PROFILE_ID_EXTRA_KEY, -1)
    val binding = DataBindingUtil.setContentView<PinPasswordActivityBinding>(
      activity,
      R.layout.pin_password_activity
    )
    pinViewModel.setProfileId(profileId)
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
              .loginToProfile(
                ProfileId.newBuilder().setInternalId(profileId).build()
              ).toLiveData()
              .observe(
                activity,
                {
                  if (it is AsyncResult.Success) {
                    activity.startActivity((HomeActivity.createHomeActivity(activity, profileId)))
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
      profileId,
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

  private fun getPinPasswordViewModel(): PinPasswordViewModel {
    return viewModelProvider.getForActivity(activity, PinPasswordViewModel::class.java)
  }

  private fun showAdminForgotPin() {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    pinViewModel.showAdminPinForgotPasswordPopUp.set(true)
    alertDialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setTitle(R.string.pin_password_forgot_title)
      .setMessage(
        resourceHandler.getStringInLocaleWithWrapping(R.string.pin_password_forgot_message, appName)
      )
      .setNegativeButton(R.string.admin_settings_cancel) { dialog, _ ->
        pinViewModel.showAdminPinForgotPasswordPopUp.set(false)
        dialog.dismiss()
      }
      .setPositiveButton(R.string.pin_password_play_store) { dialog, _ ->
        pinViewModel.showAdminPinForgotPasswordPopUp.set(false)
        try {
          activity.startActivity(
            Intent(
              Intent.ACTION_VIEW,
              Uri.parse("market://details?id=" + activity.packageName)
            )
          )
        } catch (e: ActivityNotFoundException) {
          activity.startActivity(
            Intent(
              Intent.ACTION_VIEW,
              Uri.parse(
                "https://play.google.com/store/apps/details?id=" + activity.packageName
              )
            )
          )
        }
        dialog.dismiss()
      }.create()
    alertDialog.show()
  }

  fun dismissAlertDialog() {
    if (::alertDialog.isInitialized && alertDialog.isShowing) {
      alertDialog.dismiss()
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

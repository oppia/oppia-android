package org.oppia.android.app.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.utility.LifecycleSafeTimerFactory
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.PinPasswordActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.statusbar.StatusBarColor
import javax.inject.Inject

private const val TAG_ADMIN_SETTINGS_DIALOG = "ADMIN_SETTINGS_DIALOG"
private const val TAG_RESET_PIN_DIALOG = "RESET_PIN_DIALOG"

/** The presenter for [PinPasswordActivity]. */
class PinPasswordActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  private val viewModelProvider: ViewModelProvider<PinPasswordViewModel>
) {
  private val pinViewModel by lazy {
    getPinPasswordViewModel()
  }
  private var profileId = -1
  private lateinit var alertDialog: AlertDialog

  private lateinit var binding: PinPasswordActivityBinding

  fun handleOnCreate() {
    StatusBarColor.statusBarColorUpdate(R.color.pinInputStatusBar, activity, true)
    val adminPin = activity.intent.getStringExtra(PIN_PASSWORD_ADMIN_PIN_EXTRA_KEY)
    profileId = activity.intent.getIntExtra(PIN_PASSWORD_PROFILE_ID_EXTRA_KEY, -1)
    binding = DataBindingUtil.setContentView<PinPasswordActivityBinding>(
      activity,
      R.layout.pin_password_activity
    )
    pinViewModel.setProfileId(profileId)
    binding.apply {
      lifecycleOwner = activity
      viewModel = pinViewModel
    }

    binding.showPin.setOnClickListener {
      pinViewModel.showPassword.set(!pinViewModel.showPassword.get()!!)
    }
    binding.inputPin.requestFocus()
    binding.inputPin.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(pin: CharSequence?, start: Int, before: Int, count: Int) {
        pin?.let { inputtedPin ->
          if (inputtedPin.isNotEmpty()) {
            pinViewModel.showError.set(false)
          }
          if (inputtedPin.length == pinViewModel.correctPin.get()!!.length &&
            inputtedPin.isNotEmpty() && pinViewModel.correctPin.get()!!
              .isNotEmpty()
          ) {
            if (inputtedPin.toString() == pinViewModel.correctPin.get()) {
              profileManagementController
                .loginToProfile(
                  ProfileId.newBuilder().setInternalId(profileId).build()
                ).toLiveData()
                .observe(
                  activity,
                  Observer {
                    if (it.isSuccess()) {
                      activity.startActivity((HomeActivity.createHomeActivity(activity, profileId)))
                    }
                  }
                )
            } else {
              binding.inputPin.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake))
              lifecycleSafeTimerFactory.createTimer(1000).observe(
                activity,
                Observer {
                  binding.inputPin.setText("")
                }
              )
              pinViewModel.showError.set(true)
            }
          }
        }
      }

      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    binding.forgotPin.setOnClickListener {
      if (pinViewModel.isAdmin.get()!!) {
        showAdminForgotPin()
      } else {
        val previousFrag =
          activity.supportFragmentManager.findFragmentByTag(TAG_ADMIN_SETTINGS_DIALOG)
        if (previousFrag != null) {
          activity.supportFragmentManager.beginTransaction().remove(previousFrag).commitNow()
        }
        val dialogFragment = AdminSettingsDialogFragment.newInstance(adminPin)
        dialogFragment.showNow(activity.supportFragmentManager, TAG_ADMIN_SETTINGS_DIALOG)
      }
    }

    binding.pin0EditText!!.addTextChangedListener(
      GenericTextWatcher(
        binding.pin0EditText!!,
        binding.pin1EditText
      )
    )
    binding.pin1EditText!!.addTextChangedListener(
      GenericTextWatcher(
        binding.pin1EditText!!,
        binding.pin2EditText
      )
    )
    binding.pin2EditText!!.addTextChangedListener(
      GenericTextWatcher(
        binding.pin2EditText!!,
        binding.pin3EditText
      )
    )
    binding.pin3EditText!!.addTextChangedListener(
      GenericTextWatcher(
        binding.pin3EditText!!,
        binding.pin4EditText
      )
    )
    binding.pin4EditText!!.addTextChangedListener(GenericTextWatcher(binding.pin4EditText!!, null))

    binding.pin0EditText!!.setOnKeyListener(GenericKeyEvent(binding.pin0EditText!!, null))
    binding.pin1EditText!!.setOnKeyListener(
      GenericKeyEvent(
        binding.pin1EditText!!,
        binding.pin0EditText!!
      )
    )
    binding.pin2EditText!!.setOnKeyListener(
      GenericKeyEvent(
        binding.pin2EditText!!,
        binding.pin1EditText!!
      )
    )
    binding.pin3EditText!!.setOnKeyListener(
      GenericKeyEvent(
        binding.pin3EditText!!,
        binding.pin2EditText!!
      )
    )
    binding.pin4EditText!!.setOnKeyListener(
      GenericKeyEvent(
        binding.pin4EditText!!,
        binding.pin3EditText!!
      )
    )

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
    pinViewModel.showAdminPinForgotPasswordPopUp.set(true)
    alertDialog = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setTitle(R.string.pin_password_forgot_title)
      .setMessage(R.string.pin_password_forgot_message)
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

  private fun setPinInViewModel() {
    val currentPin = binding.pin0EditText!!.text.toString() +
      binding.pin1EditText!!.text.toString() +
      binding.pin2EditText!!.text.toString() +
      binding.pin3EditText!!.text.toString() +
      binding.pin4EditText!!.text.toString()

    pinViewModel.inputPin.set(currentPin)
  }

  private fun verifyPin(inputtedPin: String) {
    if (inputtedPin.isNotEmpty()) {
      pinViewModel.showError.set(false)
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
            Observer {
              if (it.isSuccess()) {
                activity.startActivity((HomeActivity.createHomeActivity(activity, profileId)))
              }
            }
          )
      } else {
        binding.inputPin.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake))
        lifecycleSafeTimerFactory.createTimer(1000).observe(
          activity,
          Observer {
            binding.inputPin.setText("")
          }
        )
        pinViewModel.showError.set(true)
      }
    }
  }

  private fun showSuccessDialog() {
    AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setMessage(R.string.pin_password_success)
      .setPositiveButton(R.string.pin_password_close) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }
}

class GenericKeyEvent internal constructor(
  private val currentView: EditText,
  private val previousView: EditText?
) : View.OnKeyListener {
  override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
    if (event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.pin_0_edit_text && currentView.text.isEmpty()) {
      //If current is empty then previous EditText's number will also be deleted
      previousView!!.text = null
      previousView.requestFocus()
      return true
    }
    return false
  }
}

class GenericTextWatcher internal constructor(
  private val currentView: View,
  private val nextView: View?
) : TextWatcher {
  override fun afterTextChanged(editable: Editable) {
    val text = editable.toString()
    when (currentView.id) {
      R.id.pin_0_edit_text -> if (text.length == 1) nextView!!.requestFocus()
      R.id.pin_1_edit_text -> if (text.length == 1) nextView!!.requestFocus()
      R.id.pin_2_edit_text -> if (text.length == 1) nextView!!.requestFocus()
      R.id.pin_3_edit_text -> if (text.length == 1) nextView!!.requestFocus()
      R.id.pin_4_edit_text -> if (text.length == 1) nextView!!.requestFocus()
    }
  }

  override fun beforeTextChanged(
    arg0: CharSequence,
    arg1: Int,
    arg2: Int,
    arg3: Int
  ) {
  }

  override fun onTextChanged(
    arg0: CharSequence,
    arg1: Int,
    arg2: Int,
    arg3: Int
  ) {
  }
}

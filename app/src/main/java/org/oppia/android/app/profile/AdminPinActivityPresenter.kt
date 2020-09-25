package org.oppia.android.app.profile

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.AdminPinActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [AdminPinActivity]. */
@ActivityScope
class AdminPinActivityPresenter @Inject constructor(
  private val context: Context,
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<AdminPinViewModel>
) {

  private var inputtedPin = false
  private var inputtedConfirmPin = false

  private val adminViewModel by lazy {
    getAdminPinViewModel()
  }

  /** Binds ViewModel and sets up text and button listeners. */
  fun handleOnCreate() {
    activity.title = activity.getString(R.string.add_profile_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

    val binding =
      DataBindingUtil.setContentView<AdminPinActivityBinding>(activity, R.layout.admin_pin_activity)

    binding.apply {
      lifecycleOwner = activity
      viewModel = adminViewModel
    }

    binding.inputPin.post {
      addTextChangedListener(binding.inputPin) { pin ->
        pin?.let {
          adminViewModel.pinErrorMsg.set("")
          adminViewModel.savedPin.set(it.toString())
          inputtedPin = pin.isNotEmpty()
          setValidPin()
        }
      }
    }

    binding.inputConfirmPin.post {
      addTextChangedListener(binding.inputConfirmPin) { confirmPin ->
        confirmPin?.let {
          adminViewModel.confirmPinErrorMsg.set("")
          adminViewModel.savedConfirmPin.set(it.toString())
          inputtedConfirmPin = confirmPin.isNotEmpty()
          setValidPin()
        }
      }
    }

    binding.inputPin.setInput(adminViewModel.savedPin.get().toString())
    binding.inputConfirmPin.setInput(adminViewModel.savedConfirmPin.get().toString())

    binding.inputConfirmPin.addEditorActionListener(
      TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          binding.submitButton.callOnClick()
        }
        false
      }
    )

    binding.submitButton.setOnClickListener {
      val inputPin = binding.inputPin.getInput()
      val confirmPin = binding.inputConfirmPin.getInput()
      var failed = false
      if (inputPin.length < 5) {
        adminViewModel.pinErrorMsg.set(
          activity.getString(
            R.string.admin_pin_error_pin_length
          )
        )
        failed = true
      }
      if (inputPin != confirmPin) {
        adminViewModel.confirmPinErrorMsg.set(
          activity.getString(
            R.string.admin_pin_error_pin_confirm_wrong
          )
        )
        failed = true
      }
      if (failed) {
        return@setOnClickListener
      }
      val profileId =
        ProfileId.newBuilder()
          .setInternalId(activity.intent.getIntExtra(KEY_ADMIN_PIN_PROFILE_ID, -1))
          .build()

      profileManagementController.updatePin(profileId, inputPin).observe(
        activity,
        Observer {
          if (it.isSuccess()) {
            when (activity.intent.getIntExtra(KEY_ADMIN_PIN_ENUM, 0)) {
              AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value -> {
                activity.startActivity(
                  AdministratorControlsActivity.createAdministratorControlsActivityIntent(
                    context, activity.intent.getIntExtra(KEY_ADMIN_PIN_PROFILE_ID, -1)
                  )
                )
              }
              AdminAuthEnum.PROFILE_ADD_PROFILE.value -> {
                activity.startActivity(
                  AddProfileActivity.createAddProfileActivityIntent(
                    context,
                    activity.intent.getIntExtra(
                      KEY_ADMIN_PIN_COLOR_RGB, -10710042
                    )
                  )
                )
              }
            }
          }
        }
      )
    }
  }

  private fun addTextChangedListener(
    profileInputView: ProfileInputView,
    onTextChanged: (CharSequence?) -> Unit
  ) {
    profileInputView.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
      override fun afterTextChanged(p0: Editable?) {
        onTextChanged(p0)
      }

      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    })
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

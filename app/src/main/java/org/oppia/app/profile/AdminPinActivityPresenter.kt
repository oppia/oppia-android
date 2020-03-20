package org.oppia.app.profile

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AdminPinActivityBinding
import org.oppia.app.model.ProfileId
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [AdminPinActivity]. */
@ActivityScope
class AdminPinActivityPresenter @Inject constructor(
  private val context: Context,
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<AdminPinViewModel>
) {
  private val adminViewModel by lazy {
    getAdminPinViewModel()
  }

  /** Binds ViewModel and sets up text and button listeners. */
  fun handleOnCreate() {
    activity.title = activity.getString(R.string.add_profile_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

    val binding = DataBindingUtil.setContentView<AdminPinActivityBinding>(activity, R.layout.admin_pin_activity)

    binding.apply {
      lifecycleOwner = activity
      viewModel = adminViewModel
    }

    binding.inputPin.post {
      addTextChangedListener(binding.inputPin) { pin ->
        pin?.let {
          adminViewModel.pinErrorMsg.value = ""
          adminViewModel.savedPin.value = it.toString()
        }
      }
    }

    binding.inputConfirmPin.post {
      addTextChangedListener(binding.inputConfirmPin) { confirmPin ->
        confirmPin?.let {
          adminViewModel.confirmPinErrorMsg.value = ""
          adminViewModel.savedConfirmPin.value = it.toString()
        }
      }
    }

    binding.inputPin.setInput(adminViewModel.savedPin.value.toString())
    binding.inputConfirmPin.setInput(adminViewModel.savedConfirmPin.value.toString())

    binding.submitButton.setOnClickListener {
      val inputPin = binding.inputPin.getInput()
      val confirmPin = binding.inputConfirmPin.getInput()
      var failed = false
      if (inputPin.length < 5) {
        adminViewModel.pinErrorMsg.value = activity.getString(R.string.admin_pin_error_pin_length)
        failed = true
      }
      if (inputPin != confirmPin) {
        adminViewModel.confirmPinErrorMsg.value = activity.getString(R.string.admin_pin_error_pin_confirm_wrong)
        failed = true
      }
      if (failed) {
        return@setOnClickListener
      }
      val profileId =
        ProfileId.newBuilder()
          .setInternalId(activity.intent.getIntExtra(KEY_ADMIN_PIN_PROFILE_ID, -1))
          .build()

      profileManagementController.updatePin(profileId, inputPin).observe(activity, Observer {
        if (it.isSuccess()) {
          activity.startActivity(
            AddProfileActivity.createAddProfileActivityIntent(
              context, activity.intent.getIntExtra(KEY_ADMIN_PIN_COLOR_RGB, -10710042)
            )
          )
        }
      })
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

  private fun getAdminPinViewModel(): AdminPinViewModel {
    return viewModelProvider.getForActivity(activity, AdminPinViewModel::class.java)
  }
}

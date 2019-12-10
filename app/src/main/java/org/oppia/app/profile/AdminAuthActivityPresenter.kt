package org.oppia.app.profile

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AdminAuthActivityBinding
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [AdminAuthActivity]. */
@ActivityScope
class AdminAuthActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<AdminAuthViewModel>
) {
  private val authViewModel by lazy {
    getAdminAuthViewModel()
  }

  /** Binds ViewModel and sets up text and button listeners. */
  fun handleOnCreate() {
    activity.title = activity.getString(R.string.add_profile_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

    val binding = DataBindingUtil.setContentView<AdminAuthActivityBinding>(activity, R.layout.admin_auth_activity)
    val adminPin = activity.intent.getStringExtra(KEY_ADMIN_AUTH_ADMIN_PIN)
    binding.apply {
      lifecycleOwner = activity
      viewModel = authViewModel
    }

    binding.inputPin.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(confirmPin: CharSequence?, start: Int, before: Int, count: Int) {
        confirmPin?.let {
          authViewModel.errorMessage.set("")
        }
      }

      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    binding.submitButton.setOnClickListener {
      val inputPin = binding.inputPin.getInput()
      if (inputPin.isEmpty()) {
        return@setOnClickListener
      }
      if (inputPin == adminPin) {
        activity.startActivity(Intent(activity, AddProfileActivity::class.java))
      } else {
        authViewModel.errorMessage.set(activity.resources.getString(R.string.admin_auth_incorrect))
      }
    }
  }

  private fun getAdminAuthViewModel(): AdminAuthViewModel {
    return viewModelProvider.getForActivity(activity, AdminAuthViewModel::class.java)
  }
}

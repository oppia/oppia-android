package org.oppia.app.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AdminAuthActivityBinding
import javax.inject.Inject

/** The presenter for [AdminAuthActivity]. */
@ActivityScope
class AdminAuthActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<AdminAuthActivityBinding>(activity, R.layout.admin_auth_activity)
    val adminPin = activity.intent.getStringExtra(KEY_ADMIN_PIN)
    binding.submitButton.setOnClickListener {
      if (binding.inputPin.text.toString() == adminPin) {
        activity.startActivity(Intent(activity, AddProfileActivity::class.java))
      }
    }
  }
}

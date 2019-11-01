package org.oppia.app.profile

import android.content.Intent
import android.widget.Toast
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
    activity.title = "Add Profile"
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

    val binding = DataBindingUtil.setContentView<AdminAuthActivityBinding>(activity, R.layout.admin_auth_activity)
    val adminPin = activity.intent.getStringExtra(KEY_ADMIN_PIN)
    binding.submitButton.setOnClickListener {
      val inputPin = binding.inputPin.text.toString()
      if (inputPin.isEmpty()) {
        return@setOnClickListener
      }
      if (inputPin == adminPin) {
        activity.startActivity(Intent(activity, AddProfileActivity::class.java))
      } else {
        Toast.makeText(activity, activity.resources.getString(R.string.incorrect_admin_pin), Toast.LENGTH_SHORT).show()
      }
    }
  }
}

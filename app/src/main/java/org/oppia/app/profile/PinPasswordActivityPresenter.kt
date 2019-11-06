package org.oppia.app.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.databinding.PinPasswordActivityBinding
import javax.inject.Inject

class PinPasswordActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<PinPasswordActivityBinding>(activity, R.layout.pin_password_activity)
  }
}
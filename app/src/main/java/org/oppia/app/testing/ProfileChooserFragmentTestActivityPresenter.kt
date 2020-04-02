package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import javax.inject.Inject

class ProfileChooserFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.profile_chooser_test_activity)
  }
}

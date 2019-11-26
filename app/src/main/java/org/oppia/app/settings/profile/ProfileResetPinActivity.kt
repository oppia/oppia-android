package org.oppia.app.settings.profile

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that allows user to change a profile's PIN. */
class ProfileResetPinActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileResetPinActivityPresenter: ProfileResetPinActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileResetPinActivityPresenter.handleOnCreate()
  }
}

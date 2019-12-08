package org.oppia.app.settings.profile

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that allows user to select a profile to edit from settings. */
class ProfileEditActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileEditActivityPresenter: ProfileEditActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileEditActivityPresenter.handleOnCreate()
  }
}

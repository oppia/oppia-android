package org.oppia.app.profile

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class ProfileActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileActivityPresenter: ProfileActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileActivityPresenter.handleOnCreate()
  }
}
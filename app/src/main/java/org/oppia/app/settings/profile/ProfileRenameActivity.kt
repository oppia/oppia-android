package org.oppia.app.settings.profile

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that allows user to rename a profile. */
class ProfileRenameActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileRenameActivityPresenter: ProfileRenameActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileRenameActivityPresenter.handleOnCreate()
  }
}

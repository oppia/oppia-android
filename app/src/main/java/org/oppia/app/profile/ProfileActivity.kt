package org.oppia.app.profile

import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that controls profile creation and selection. */
class ProfileActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileActivityPresenter: ProfileActivityPresenter

  @ExperimentalCoroutinesApi
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileActivityPresenter.handleOnCreate()
  }
}

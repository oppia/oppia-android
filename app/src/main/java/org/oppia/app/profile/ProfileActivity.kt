package org.oppia.app.profile

import android.content.Context
import android.content.Intent
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

  companion object {
    /** Returns a new [Intent] to route to [ProfileActivity]. */
    fun createProfileActivityIntent(context: Context): Intent {
      return Intent(context, ProfileActivity::class.java)
    }
  }
}

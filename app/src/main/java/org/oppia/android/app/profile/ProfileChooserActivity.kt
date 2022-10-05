package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.PROFILE_CHOOSER_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that controls profile creation and selection. */
class ProfileChooserActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileChooserActivityPresenter: ProfileChooserActivityPresenter

  companion object {
    fun createProfileChooserActivity(context: Context): Intent {
      return Intent(context, ProfileChooserActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        decorateWithScreenName(PROFILE_CHOOSER_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileChooserActivityPresenter.handleOnCreate()
  }
}

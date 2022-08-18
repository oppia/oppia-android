package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator
import javax.inject.Inject

/** Activity that controls profile creation and selection. */
class ProfileChooserActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileChooserActivityPresenter: ProfileChooserActivityPresenter

  companion object {
    fun createProfileChooserActivity(context: Context): Intent {
      val intent = Intent(context, ProfileChooserActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      intent.putProtoExtra(
        CurrentAppScreenNameIntentDecorator.getCurrentAppScreenNameIntentKey(),
        CurrentAppScreenNameIntentDecorator.decorateWithScreenName(
          ScreenName.PROFILE_CHOOSER_ACTIVITY
        )
      )
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileChooserActivityPresenter.handleOnCreate()
  }
}

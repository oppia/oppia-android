package org.oppia.android.app.mydownloads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.MY_DOWNLOADS_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity for displaying [MyDownloadsFragment]. */
class MyDownloadsActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var myDownloadsActivityPresenter: MyDownloadsActivityPresenter
  private lateinit var profileId: ProfileId

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    myDownloadsActivityPresenter.handleOnCreate()
    profileId = intent.extractCurrentUserProfileId()
  }

  companion object {
    fun createMyDownloadsActivityIntent(context: Context, profileId: ProfileId): Intent {
      val intent = Intent(context, MyDownloadsActivity::class.java)
      intent.decorateWithScreenName(MY_DOWNLOADS_ACTIVITY)
      intent.decorateWithUserProfileId(profileId)
      return intent
    }
  }

  override fun onBackPressed() {
    val intent = HomeActivity.createHomeActivity(this, profileId)
    startActivity(intent)
    finish()
  }
}

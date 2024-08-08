package org.oppia.android.app.mydownloads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.MY_DOWNLOADS_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity for displaying [MyDownloadsFragment]. */
class MyDownloadsActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var myDownloadsActivityPresenter: MyDownloadsActivityPresenter

  @Inject
  @EnableMultipleClassrooms
  lateinit var enableMultipleClassrooms: PlatformParameterValue<Boolean>

  private var internalProfileId: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    myDownloadsActivityPresenter.handleOnCreate()
    internalProfileId = intent?.extractCurrentUserProfileId()?.loggedInInternalProfileId ?: -1
  }

  companion object {
    fun createMyDownloadsActivityIntent(context: Context, internalProfileId: Int?): Intent {
      val profileId = internalProfileId?.let {
        ProfileId.newBuilder().setLoggedInInternalProfileId(it).build()
      }
      val intent = Intent(context, MyDownloadsActivity::class.java)
      if (profileId != null) {
        intent.decorateWithUserProfileId(profileId)
      }
      intent.decorateWithScreenName(MY_DOWNLOADS_ACTIVITY)
      return intent
    }
  }

  override fun onBackPressed() {
    val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    val intent = if (enableMultipleClassrooms.value)
      ClassroomListActivity.createClassroomListActivity(this, profileId)
    else
      HomeActivity.createHomeActivity(this, profileId)
    startActivity(intent)
    finish()
  }
}

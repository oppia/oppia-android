package org.oppia.android.app.mydownloads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.model.ScreenName.MY_DOWNLOADS_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.HomeActivityParams

/** The activity for displaying [MyDownloadsFragment]. */
class MyDownloadsActivity : InjectableAppCompatActivity() {
  @Inject lateinit var myDownloadsActivityPresenter: MyDownloadsActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  private var internalProfileId: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    myDownloadsActivityPresenter.handleOnCreate()
    internalProfileId = intent.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)
  }

  companion object {
    fun createIntent(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, MyDownloadsActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
      intent.decorateWithScreenName(MY_DOWNLOADS_ACTIVITY)
      return intent
    }

    fun getIntentKey(): String {
      return NAVIGATION_PROFILE_ID_ARGUMENT_KEY
    }
  }

  override fun onBackPressed() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        homeActivityParams = HomeActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
        }.build()
      }.build()
    )
    finish()
  }

  interface Injector {
    fun inject(activity: MyDownloadsActivity)
  }
}

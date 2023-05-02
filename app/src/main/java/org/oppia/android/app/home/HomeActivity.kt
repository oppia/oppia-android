package org.oppia.android.app.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.drawer.ExitProfileDialogFragment
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.drawer.TAG_SWITCH_PROFILE_DIALOG
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ExitProfileDialogArguments
import org.oppia.android.app.model.HighlightItem
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.model.ScreenName.HOME_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject
import org.oppia.android.app.model.TopicActivityParams

/** The central activity for all users entering the app. */
class HomeActivity :
  InjectableAppCompatActivity(),
  RouteToTopicListener,
  RouteToTopicPlayStoryListener,
  RouteToRecentlyPlayedListener {
  @Inject lateinit var homeActivityPresenter: HomeActivityPresenter
  @Inject lateinit var resourceHandler: AppLanguageResourceHandler
  @Inject lateinit var activityRouter: ActivityRouter

  private var internalProfileId: Int = -1

  companion object {
    fun createIntent(context: Context, profileId: Int?): Intent {
      return Intent(context, HomeActivity::class.java).apply {
        putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
        decorateWithScreenName(HOME_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    internalProfileId = intent?.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)!!
    homeActivityPresenter.handleOnCreate(internalProfileId)
    title = resourceHandler.getStringInLocale(R.string.home_activity_title)
  }

  override fun onRestart() {
    super.onRestart()
    homeActivityPresenter.handleOnRestart()
  }

  override fun routeToTopic(internalProfileId: Int, topicId: String) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        topicActivityParams = TopicActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
          this.topicId = topicId
          this.storyList = true
        }.build()
      }.build()
    )
  }

  override fun onBackPressed() {
    val previousFragment =
      supportFragmentManager.findFragmentByTag(TAG_SWITCH_PROFILE_DIALOG)
    if (previousFragment != null) {
      supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val exitProfileDialogArguments =
      ExitProfileDialogArguments
        .newBuilder()
        .setHighlightItem(HighlightItem.NONE)
        .build()
    val dialogFragment = ExitProfileDialogFragment
      .newInstance(exitProfileDialogArguments = exitProfileDialogArguments)
    dialogFragment.showNow(supportFragmentManager, TAG_SWITCH_PROFILE_DIALOG)
  }

  override fun routeToTopicPlayStory(internalProfileId: Int, topicId: String, storyId: String) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        topicActivityParams = TopicActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
          this.topicId = topicId
          this.specificStoryId = storyId
        }.build()
      }.build()
    )
  }

  override fun routeToRecentlyPlayed(recentlyPlayedActivityTitle: RecentlyPlayedActivityTitle) {
    val recentlyPlayedActivityParams =
      RecentlyPlayedActivityParams
        .newBuilder()
        .setProfileId(ProfileId.newBuilder().setInternalId(internalProfileId).build())
        .setActivityTitle(recentlyPlayedActivityTitle).build()

    activityRouter.routeToScreen(
      DestinationScreen
        .newBuilder()
        .setRecentlyPlayedActivityParams(recentlyPlayedActivityParams)
        .build()
    )
  }

  interface Injector {
    fun inject(activity: HomeActivity)
  }
}

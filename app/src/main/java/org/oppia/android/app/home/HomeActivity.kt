package org.oppia.android.app.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
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
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity :
  InjectableAppCompatActivity(),
  RouteToTopicListener,
  RouteToTopicPlayStoryListener,
  RouteToRecentlyPlayedListener {
  @Inject
  lateinit var homeActivityPresenter: HomeActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  @Inject
  lateinit var activityRouter: ActivityRouter

  private var internalProfileId: Int = -1

  companion object {
    fun createHomeActivity(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, HomeActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent?.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)!!
    homeActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.home_activity_title)
  }

  override fun onRestart() {
    super.onRestart()
    homeActivityPresenter.handleOnRestart()
  }

  override fun routeToTopic(internalProfileId: Int, topicId: String) {
    startActivity(TopicActivity.createTopicActivityIntent(this, internalProfileId, topicId))
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
    startActivity(
      TopicActivity.createTopicPlayStoryActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId
      )
    )
  }

  override fun routeToRecentlyPlayed(title: String) {
    val recentlyPlayedActivityParams =
      RecentlyPlayedActivityParams
        .newBuilder()
        .setProfileId(ProfileId.newBuilder().setInternalId(internalProfileId).build())
        .setActivityTitle(
          when (title) {
            resourceHandler.getStringInLocale(R.string.stories_for_you) -> {
              RecentlyPlayedActivityTitle.STORIES_FOR_YOU
            }
            resourceHandler.getStringInLocale(R.string.recently_played_activity) -> {
              RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES
            }
            else -> {
              RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES
            }
          }
        ).build()

    activityRouter.routeToScreen(
      DestinationScreen
        .newBuilder()
        .setRecentlyPlayedActivityParams(recentlyPlayedActivityParams)
        .build()
    )
  }
}

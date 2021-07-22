package org.oppia.android.app.home

import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import hotchemi.android.rate.AppRate
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.ExitProfileDialogFragment
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.drawer.TAG_SWITCH_PROFILE_DIALOG
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.ExitProfileDialogArguments
import org.oppia.android.app.model.HighlightItem
import org.oppia.android.app.topic.TopicActivity

/** The central activity for all users entering the app. */
class HomeActivity :
  InjectableAppCompatActivity(),
  RouteToTopicListener,
  RouteToTopicPlayStoryListener,
  RouteToRecentlyPlayedListener {
  @Inject
  lateinit var homeActivityPresenter: HomeActivityPresenter
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
    activityComponent.inject(this)
    internalProfileId = intent?.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)!!
    homeActivityPresenter.handleOnCreate()
    title = getString(R.string.menu_home)
    setUpRateApp()
  }

  private fun setUpRateApp() {
    AppRate.with(this)
      .setInstallDays(7) // Specifies the number of days after installation, the dialog popup shows.
      .setLaunchTimes(2) // Specifies the number of times the app must launch by user for the dialog popup to show.
      .setRemindInterval(3) // Specifies the number of days after "Remind Me Later" is clicked, the dialog popup will show.
      .setShowLaterButton(true)
      .setDebug(false) // IMPORTANT: Set true only for testing purposes. DO NOT set true for release-app.
      .setOnClickButtonListener {
        // Space to alter the entire functionality of the "dialog popup" for future purposes.
      }
      .monitor()

    //To show the dialog popup only if meets ALL specified conditions.

    //To show the dialog popup only if meets ALL specified conditions.
    AppRate.showRateDialogIfMeetsConditions(this)
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

  override fun routeToRecentlyPlayed() {
    startActivity(
      RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
        this,
        internalProfileId
      )
    )
  }
}

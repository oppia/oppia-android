package org.oppia.android.app.classroom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.drawer.ExitProfileDialogFragment
import org.oppia.android.app.drawer.TAG_SWITCH_PROFILE_DIALOG
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ExitProfileDialogArguments
import org.oppia.android.app.model.HighlightItem
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.model.ScreenName.CLASSROOM_LIST_ACTIVITY
import org.oppia.android.app.topic.TopicActivity.Companion.createTopicActivityIntent
import org.oppia.android.app.topic.TopicActivity.Companion.createTopicPlayStoryActivityIntent
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity for displaying [ClassroomListFragment]. */
class ClassroomListActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToTopicListener,
  RouteToTopicPlayStoryListener,
  RouteToRecentlyPlayedListener {
  @Inject
  lateinit var classroomListActivityPresenter: ClassroomListActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  @Inject
  lateinit var activityRouter: ActivityRouter

  private var internalProfileId: Int = -1

  companion object {
    /** Returns a new [Intent] to route to [ClassroomListActivity] for a specified [profileId]. */
    fun createClassroomListActivity(context: Context, profileId: ProfileId?): Intent {
      return Intent(context, ClassroomListActivity::class.java).apply {
        decorateWithScreenName(CLASSROOM_LIST_ACTIVITY)
        profileId?.let { decorateWithUserProfileId(profileId) }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    internalProfileId = intent.extractCurrentUserProfileId().loggedInInternalProfileId
    classroomListActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.classroom_list_activity_title)
  }

  override fun onRestart() {
    super.onRestart()
    classroomListActivityPresenter.handleOnRestart()
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

  override fun routeToRecentlyPlayed(recentlyPlayedActivityTitle: RecentlyPlayedActivityTitle) {
    val recentlyPlayedActivityParams =
      RecentlyPlayedActivityParams
        .newBuilder()
        .setProfileId(
          ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
        )
        .setActivityTitle(recentlyPlayedActivityTitle).build()

    activityRouter.routeToScreen(
      DestinationScreen
        .newBuilder()
        .setRecentlyPlayedActivityParams(recentlyPlayedActivityParams)
        .build()
    )
  }

  override fun routeToTopic(internalProfileId: Int, classroomId: String, topicId: String) {
    startActivity(
      createTopicActivityIntent(this, internalProfileId, classroomId, topicId)
    )
  }

  override fun routeToTopicPlayStory(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String
  ) {
    startActivity(
      createTopicPlayStoryActivityIntent(
        this,
        internalProfileId,
        classroomId,
        topicId,
        storyId
      )
    )
  }
}

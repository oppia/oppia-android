package org.oppia.android.app.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.drawer.ExitProfileDialogFragment
import org.oppia.android.app.drawer.TAG_SWITCH_PROFILE_DIALOG
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ExitProfileDialogArguments
import org.oppia.android.app.model.HighlightItem
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.model.ScreenName.HOME_ACTIVITY
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToTopicListener,
  RouteToTopicPlayStoryListener,
  RouteToRecentlyPlayedListener,
  ExitProfileListener {
  @Inject
  lateinit var homeActivityPresenter: HomeActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  @Inject
  lateinit var activityRouter: ActivityRouter

  @Inject
  @field:EnableOnboardingFlowV2
  lateinit var enableOnboardingFlowV2: PlatformParameterValue<Boolean>

  private var internalProfileId: Int = -1

  companion object {

    fun createHomeActivity(context: Context, profileId: ProfileId?): Intent {
      return Intent(context, HomeActivity::class.java).apply {
        decorateWithScreenName(HOME_ACTIVITY)
        if (profileId != null) {
          decorateWithUserProfileId(profileId)
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    internalProfileId = intent.extractCurrentUserProfileId().internalId
    homeActivityPresenter.handleOnCreate(internalProfileId)
    title = resourceHandler.getStringInLocale(R.string.home_activity_title)
  }

  override fun routeToTopic(profileId: ProfileId, classroomId: String, topicId: String) {
    startActivity(
      TopicActivity.createTopicActivityIntent(this, profileId, classroomId, topicId)
    )
  }

  override fun routeToTopicPlayStory(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String
  ) {
    startActivity(
      TopicActivity.createTopicPlayStoryActivityIntent(
        this,
        profileId,
        classroomId,
        topicId,
        storyId
      )
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

  override fun exitProfile(profileType: ProfileType) {
    val previousFragment =
      supportFragmentManager.findFragmentByTag(TAG_SWITCH_PROFILE_DIALOG)
    if (previousFragment != null) {
      supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val exitProfileDialogArguments =
      ExitProfileDialogArguments
        .newBuilder().apply {
          if (enableOnboardingFlowV2.value) {
            this.profileType = profileType
          }
          this.highlightItem = HighlightItem.NONE
        }
        .build()
    val dialogFragment = ExitProfileDialogFragment
      .newInstance(exitProfileDialogArguments = exitProfileDialogArguments)
    dialogFragment.showNow(supportFragmentManager, TAG_SWITCH_PROFILE_DIALOG)
  }
}

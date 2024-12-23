package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.home.ExitProfileListener
import org.oppia.android.app.home.HomeActivityPresenter
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

class NavigationDrawerTestActivity :
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

  private var internalProfileId: Int = -1

  companion object {
    fun createNavigationDrawerTestActivity(context: Context, internalProfileId: Int?): Intent {
      val intent = Intent(context, NavigationDrawerTestActivity::class.java)
      val profileId = internalProfileId?.let { ProfileId.newBuilder().setInternalId(it).build() }
      if (profileId != null) {
        intent.decorateWithUserProfileId(profileId)
      }
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent?.extractCurrentUserProfileId()?.internalId ?: -1
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
        .setActivityTitle(recentlyPlayedActivityTitle)
        .build()

    activityRouter.routeToScreen(
      DestinationScreen
        .newBuilder()
        .setRecentlyPlayedActivityParams(recentlyPlayedActivityParams)
        .build()
    )
  }

  override fun exitProfile(profileType: ProfileType) {}
}

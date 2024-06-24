package org.oppia.android.app.profileprogress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.completedstorylist.CompletedStoryListActivity
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.model.ScreenName.PROFILE_PROGRESS_ACTIVITY
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity to display profile progress. */
class ProfileProgressActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToCompletedStoryListListener,
  RouteToOngoingTopicListListener,
  RouteToRecentlyPlayedListener,
  ProfilePictureDialogInterface {

  @Inject
  lateinit var profileProgressActivityPresenter: ProfileProgressActivityPresenter
  private var internalProfileId = -1

  @Inject
  lateinit var activityRouter: ActivityRouter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent?.extractCurrentUserProfileId()?.internalId ?: -1
    profileProgressActivityPresenter.handleOnCreate(internalProfileId)
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

  override fun routeToCompletedStory() {
    startActivity(
      CompletedStoryListActivity.createCompletedStoryListActivityIntent(
        this,
        internalProfileId
      )
    )
  }

  override fun routeToOngoingTopic() {
    startActivity(
      OngoingTopicListActivity.createOngoingTopicListActivityIntent(
        this,
        internalProfileId
      )
    )
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.

    fun createProfileProgressActivityIntent(context: Context, internalProfileId: Int): Intent {
      val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
      return Intent(context, ProfileProgressActivity::class.java).apply {
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(PROFILE_PROGRESS_ACTIVITY)
      }
    }
  }

  override fun showProfilePicture() {
    startActivity(
      ProfilePictureActivity.createProfilePictureActivityIntent(
        this,
        internalProfileId
      )
    )
  }

  override fun showGalleryForProfilePicture() {
    profileProgressActivityPresenter.openGalleryIntent()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    profileProgressActivityPresenter.handleOnActivityResult(data)
  }
}

package org.oppia.android.app.profileprogress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.model.ScreenName.PROFILE_PROGRESS_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject
import org.oppia.android.app.model.CompletedStoryListActivityParams
import org.oppia.android.app.model.OngoingTopicListActivityParams
import org.oppia.android.app.model.ProfilePictureActivityParams

/** Activity to display profile progress. */
class ProfileProgressActivity :
  InjectableAppCompatActivity(),
  RouteToCompletedStoryListListener,
  RouteToOngoingTopicListListener,
  RouteToRecentlyPlayedListener,
  ProfilePictureDialogInterface {
  @Inject lateinit var profileProgressActivityPresenter: ProfileProgressActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter
  @Inject lateinit var resourceHandler: AppLanguageResourceHandler

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, -1)
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
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        completedStoryListActivityParams = CompletedStoryListActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
        }.build()
      }.build()
    )
  }

  override fun routeToOngoingTopic() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        ongoingTopicListActivityParams = OngoingTopicListActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
        }.build()
      }.build()
    )
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val PROFILE_ID_EXTRA_KEY = "ProfileProgressActivity.profile_id"

    fun createIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, ProfileProgressActivity::class.java).apply {
        putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
        decorateWithScreenName(PROFILE_PROGRESS_ACTIVITY)
      }
    }
  }

  override fun showProfilePicture() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        profilePictureActivityParams = ProfilePictureActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
        }.build()
      }.build()
    )
  }

  override fun showGalleryForProfilePicture() {
    profileProgressActivityPresenter.openGalleryIntent()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    profileProgressActivityPresenter.handleOnActivityResult(data)
  }

  interface Injector {
    fun inject(activity: ProfileProgressActivity)
  }
}

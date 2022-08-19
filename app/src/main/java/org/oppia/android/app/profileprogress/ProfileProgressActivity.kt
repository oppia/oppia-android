package org.oppia.android.app.profileprogress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.completedstorylist.CompletedStoryListActivity
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListActivity
import javax.inject.Inject

/** Activity to display profile progress. */
class ProfileProgressActivity :
  InjectableAppCompatActivity(),
  RouteToCompletedStoryListListener,
  RouteToOngoingTopicListListener,
  RouteToRecentlyPlayedListener,
  ProfilePictureDialogInterface {

  @Inject
  lateinit var profileProgressActivityPresenter: ProfileProgressActivityPresenter
  private var internalProfileId = -1

  @Inject
  lateinit var activityRouter: ActivityRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, -1)
    profileProgressActivityPresenter.handleOnCreate(internalProfileId)
  }

  override fun routeToRecentlyPlayed(title: String) {
    val recentlyPlayedActivityParams =
      RecentlyPlayedActivityParams
        .newBuilder()
        .setProfileId(ProfileId.newBuilder().setInternalId(internalProfileId).build())
        .setActivityTitle(
          when (title) {
            getString(R.string.stories_for_you) -> {
              RecentlyPlayedActivityTitle.STORIES_FOR_YOU
            }
            getString(R.string.recently_played_activity) -> {
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
    const val PROFILE_ID_EXTRA_KEY = "ProfileProgressActivity.profile_id"

    fun createProfileProgressActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, ProfileProgressActivity::class.java)
      intent.putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
      return intent
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

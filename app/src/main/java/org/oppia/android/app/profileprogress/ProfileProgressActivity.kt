package org.oppia.android.app.profileprogress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.completedstorylist.CompletedStoryListActivity
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.android.app.model.ScreenName.PROFILE_PROGRESS_ACTIVITY
import javax.inject.Inject
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName

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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, -1)
    profileProgressActivityPresenter.handleOnCreate(internalProfileId)
  }

  override fun routeToRecentlyPlayed() {
    startActivity(
      RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
        this,
        internalProfileId
      )
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
      return Intent(context, ProfileProgressActivity::class.java).apply {
        putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
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

package org.oppia.app.profileprogress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.completedstorylist.CompletedStoryListActivity
import org.oppia.app.home.RouteToRecentlyPlayedListener
import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.app.ongoingtopiclist.OngoingTopicListActivity
import javax.inject.Inject

/** Activity to display profile progress. */
class ProfileProgressActivity : InjectableAppCompatActivity(), RouteToCompletedStoryListListener,
  RouteToOngoingTopicListListener, RouteToRecentlyPlayedListener {

  @Inject lateinit var profileProgressActivityPresenter: ProfileProgressActivityPresenter
  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(PROFILE_PROGRESS_ACTIVITY_PROFILE_ID_KEY, -1)
    profileProgressActivityPresenter.handleOnCreate(internalProfileId)
  }

  override fun routeToRecentlyPlayed() {
    startActivity(RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(this, internalProfileId))
  }

  override fun routeToCompletedStory() {
    startActivity(CompletedStoryListActivity.createCompletedStoryListActivityIntent(this, internalProfileId))
  }

  override fun routeToOngoingTopic() {
    startActivity(OngoingTopicListActivity.createOngoingTopicListActivityIntent(this, internalProfileId))
  }

  companion object {
    internal const val PROFILE_PROGRESS_ACTIVITY_PROFILE_ID_KEY = "ProfileProgressActivity.internal_profile_id"

    fun createProfileProgressActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, ProfileProgressActivity::class.java)
      intent.putExtra(PROFILE_PROGRESS_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}

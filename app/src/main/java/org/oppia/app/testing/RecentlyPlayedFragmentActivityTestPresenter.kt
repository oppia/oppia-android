package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.home.recentlyplayed.RecentlyPlayedFragment
import org.oppia.app.model.ProfileId
import org.oppia.domain.topic.StoryProgressTestHelper
import javax.inject.Inject

/** The presenter for [RecentlyPlayedFragmentActivityTest]. */
@ActivityScope
class RecentlyPlayedFragmentActivityTestPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val storyProgressTestHelper: StoryProgressTestHelper
) {

  fun handleOnCreate() {
    val profileId = ProfileId.newBuilder().setInternalId(0).build()
    storyProgressTestHelper.markRecentlyPlayedForFractionsStory0Exploration0(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedForRatiosStory0Exploration0(
      profileId,
      timestampOlderThanAWeek = true
    )
    activity.setContentView(R.layout.recently_played_fragment_test_activity)
    if (getRecentlyPlayedFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.recently_played_fragment_placeholder,
        RecentlyPlayedFragment.newInstance(0),
        RecentlyPlayedFragmentActivityTest.TAG_RECENTLY_PLAYED_FRAGMENT
      ).commitNow()
    }
  }

  private fun getRecentlyPlayedFragment(): RecentlyPlayedFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.recently_played_fragment_placeholder) as RecentlyPlayedFragment?
  }
}
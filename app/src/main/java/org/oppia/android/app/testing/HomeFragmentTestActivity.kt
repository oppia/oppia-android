package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.home.ExitProfileListener
import org.oppia.android.app.home.HomeFragment
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.testing.activity.TestActivity

/**
 * Test Activity for testing view models on the [HomeFragment].
 * This activity must implement listeners so the tests can use it as a [HomeFragment].
 */
class HomeFragmentTestActivity :
  RouteToTopicListener,
  RouteToTopicPlayStoryListener,
  RouteToRecentlyPlayedListener,
  TestActivity(),
  ExitProfileListener {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
  }

  companion object {
    fun createHomeFragmentTestActivity(context: Context): Intent {
      return Intent(context, HomeFragmentTestActivity::class.java)
    }
  }

  // Override functions are needed to fulfill listener definitions.
  override fun routeToTopic(profileId: ProfileId, classroomId: String, topicId: String) {}
  override fun routeToTopicPlayStory(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String
  ) {}
  override fun routeToRecentlyPlayed(recentlyPlayedActivityTitle: RecentlyPlayedActivityTitle) {}
  override fun exitProfile(profileType: ProfileType) {}
}

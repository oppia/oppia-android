package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.testing.activity.TestActivity

/**
 * Test activity for testing ``HomeFragment``.
 *
 * This activity must implement the listeners that ``HomeFragment`` expects.
 */
class HomeFragmentTestActivity :
  RouteToTopicListener,
  RouteToTopicPlayStoryListener,
  RouteToRecentlyPlayedListener,
  TestActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
  }

  companion object {
    fun createIntent(context: Context): Intent =
      Intent(context, HomeFragmentTestActivity::class.java)
  }

  // Override functions are needed to fulfill listener definitions.
  override fun routeToTopic(internalProfileId: Int, topicId: String) {}
  override fun routeToTopicPlayStory(internalProfileId: Int, topicId: String, storyId: String) {}
  override fun routeToRecentlyPlayed(recentlyPlayedActivityTitle: RecentlyPlayedActivityTitle) {}

  interface Injector {
    fun inject(activity: HomeFragmentTestActivity)
  }
}

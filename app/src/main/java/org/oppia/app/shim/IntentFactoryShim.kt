package org.oppia.app.shim

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.app.topic.TopicActivity
import javax.inject.Inject

class IntentFactoryShim @Inject constructor() : IntentFactoryShimInterface {

  private val TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "TopicActivity.topic_id"
  private val TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY = "TopicActivity.story_id"

  override fun createProfileActivityIntent(fragment: FragmentActivity): Intent {
    return Intent(fragment, ProfileActivity::class.java)
  }

  override fun createTopicPlayStoryActivityIntent(
    context: Context,
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): Intent {
    val intent = Intent(context, TopicActivity::class.java)
    intent.putExtra(KEY_NAVIGATION_PROFILE_ID, internalProfileId)
    intent.putExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
    intent.putExtra(TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
    return intent
  }

  override fun createRecentlyPlayedActivityIntent(context: Context, internalProfileId: Int): Intent {
    val intent = Intent(context, RecentlyPlayedActivity::class.java)
    intent.putExtra(RecentlyPlayedActivity.RECENTLY_PLAYED_ACTIVITY_INTERNAL_PROFILE_ID_KEY, internalProfileId)
    return intent
  }
}

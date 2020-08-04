package org.oppia.app

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.profile.ProfileActivity
import org.oppia.app.topic.TopicActivity

private const val TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "TopicActivity.topic_id"
private const val TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY = "TopicActivity.story_id"

class IntentFactoryShim: IntentFactoryShimInterface {

      override fun createProfileActivityIntent(fragment: Fragment): Intent {
        return Intent(fragment.activity, ProfileActivity::class.java)
      }

      override fun createTopicActivityIntent(
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
}
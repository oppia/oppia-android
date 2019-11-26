package org.oppia.app.topic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

const val TOPIC_FRAGMENT_TAG = "TopicFragment"
const val TOPIC_ID_ARGUMENT_KEY = "topic_id"
const val STORY_ID_ARGUMENT_KEY = "story_id"

/** The presenter for [TopicActivity]. */
@ActivityScope
class TopicActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  fun handleOnCreate(topicId: String, storyId: String?) {
    activity.setContentView(R.layout.topic_activity)
    if (getTopicFragment() == null) {
      val topicFragment = TopicFragment()
      val args = Bundle()
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      if (storyId != null) {
        args.putString(STORY_ID_ARGUMENT_KEY, storyId)
      }
      topicFragment.arguments = args
      activity.supportFragmentManager.beginTransaction().add(
        R.id.topic_fragment_placeholder,
        topicFragment, TOPIC_FRAGMENT_TAG
      ).commitNow()
    }
  }

  private fun getTopicFragment(): TopicFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.topic_fragment_placeholder) as TopicFragment?
  }
}

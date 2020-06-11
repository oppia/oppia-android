package org.oppia.app.topic

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.oppia.app.topic.info.TopicInfoFragment
import org.oppia.app.topic.lessons.TopicLessonsFragment
import org.oppia.app.topic.practice.TopicPracticeFragment
import org.oppia.app.topic.revision.TopicRevisionFragment

/** Adapter to bind fragments to [FragmentStatePagerAdapter] inside [TopicFragment]. */
class ViewPagerAdapter(
  fragmentManager: FragmentManager,
  private val internalProfileId: Int,
  private val topicId: String,
  private val storyId: String
) :
  FragmentStatePagerAdapter(fragmentManager) {

  override fun getItem(position: Int): Fragment {
    val args = Bundle()
    args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
    args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
    when (TopicTab.getTabForPosition(position)) {
      TopicTab.INFO -> {
        return TopicInfoFragment.newInstance(internalProfileId, topicId)
      }
      TopicTab.LESSONS -> {
        return TopicLessonsFragment.newInstance(internalProfileId, topicId, storyId)
      }
      TopicTab.PRACTICE -> {
        return TopicPracticeFragment.newInstance(internalProfileId, topicId)
      }
      TopicTab.REVISION -> {
        return TopicRevisionFragment.newInstance(internalProfileId, topicId)
      }
    }
  }

  override fun getCount(): Int {
    return TopicTab.values().size
  }
}

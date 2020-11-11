package org.oppia.android.app.topic

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.oppia.android.app.topic.info.TopicInfoFragment
import org.oppia.android.app.topic.lessons.TopicLessonsFragment
import org.oppia.android.app.topic.practice.TopicPracticeFragment
import org.oppia.android.app.topic.revision.TopicRevisionFragment

/** Adapter to bind fragments to [FragmentStateAdapter] inside [TopicFragment]. */
class ViewPagerAdapter(
  fragment: Fragment,
  private val internalProfileId: Int,
  private val topicId: String,
  private val storyId: String
) : FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int {
    return TopicTab.values().size
  }

  override fun createFragment(position: Int): Fragment {
    return when (TopicTab.getTabForPosition(position)) {
      TopicTab.INFO -> {
        TopicInfoFragment.newInstance(internalProfileId, topicId)
      }
      TopicTab.LESSONS -> {
        TopicLessonsFragment.newInstance(internalProfileId, topicId, storyId)
      }
      TopicTab.PRACTICE -> {
        TopicPracticeFragment.newInstance(internalProfileId, topicId)
      }
      TopicTab.REVISION -> {
        TopicRevisionFragment.newInstance(internalProfileId, topicId)
      }
    }
  }
}

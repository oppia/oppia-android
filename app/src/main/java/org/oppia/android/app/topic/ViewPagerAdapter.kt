package org.oppia.android.app.topic

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/** Adapter to bind fragments to [FragmentStateAdapter] inside [TopicFragment]. */
class ViewPagerAdapter(
  fragment: Fragment,
  private val internalProfileId: Int,
  private val topicId: String,
  private val storyId: String,
  private val enableExtraTopicTabsUi: Boolean,
  private val createTopicInfoFragment: (Int, String) -> Fragment,
  private val createTopicLessonsFragment: (Int, String, String) -> Fragment,
  private val createTopicPracticeFragment: (Int, String) -> Fragment,
  private val createTopicRevisionFragment: (Int, String) -> Fragment
) : FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int = TopicTab.getTabCount(enableExtraTopicTabsUi)

  override fun createFragment(position: Int): Fragment {
    return when (TopicTab.getTabForPosition(position, enableExtraTopicTabsUi)) {
      TopicTab.INFO -> createTopicInfoFragment(internalProfileId, topicId)
      TopicTab.LESSONS -> createTopicLessonsFragment(internalProfileId, topicId, storyId)
      TopicTab.PRACTICE -> createTopicPracticeFragment(internalProfileId, topicId)
      TopicTab.REVISION -> createTopicRevisionFragment(internalProfileId, topicId)
    }
  }
}

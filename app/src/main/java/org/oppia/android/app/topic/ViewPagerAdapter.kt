package org.oppia.android.app.topic

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.info.TopicInfoFragment
import org.oppia.android.app.topic.lessons.TopicLessonsFragment
import org.oppia.android.app.topic.practice.TopicPracticeFragment
import org.oppia.android.app.topic.revision.TopicRevisionFragment

/** Adapter to bind fragments to [FragmentStateAdapter] inside [TopicFragment]. */
class ViewPagerAdapter(
  fragment: Fragment,
  private val profileId: ProfileId,
  private val classroomId: String,
  private val topicId: String,
  private val storyId: String,
  private val enableExtraTopicTabsUi: Boolean
) : FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int = TopicTab.getTabCount(enableExtraTopicTabsUi)

  override fun createFragment(position: Int): Fragment {
    return when (TopicTab.getTabForPosition(position, enableExtraTopicTabsUi)) {
      TopicTab.INFO -> {
        TopicInfoFragment.newInstance(profileId, topicId)
      }
      TopicTab.LESSONS -> {
        TopicLessonsFragment.newInstance(profileId, classroomId, topicId, storyId)
      }
      TopicTab.PRACTICE -> {
        TopicPracticeFragment.newInstance(profileId, topicId)
      }
      TopicTab.REVISION -> {
        TopicRevisionFragment.newInstance(profileId, topicId)
      }
    }
  }
}

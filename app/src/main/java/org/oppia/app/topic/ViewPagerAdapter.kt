package org.oppia.app.topic

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.oppia.app.topic.info.TopicInfoFragment
import org.oppia.app.topic.lessons.TopicLessonsFragment
import org.oppia.app.topic.review.TopicReviewFragment
import org.oppia.app.topic.practice.TopicPracticeFragment

/** Adapter to bind fragments to [FragmentStatePagerAdapter] inside [TopicFragment]. */
class ViewPagerAdapter(
  fragmentManager: FragmentManager,
  private val topicId: String,
  private val storyId: String
) :
  FragmentStatePagerAdapter(fragmentManager) {

  override fun getItem(position: Int): Fragment {
    val args = Bundle()
    args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
    when (TopicTab.getTabForPosition(position)) {
      TopicTab.INFO -> {
        val topicInfoTab = TopicInfoFragment()
        topicInfoTab.arguments = args
        return topicInfoTab
      }
      TopicTab.LESSONS -> {
        val topicPlayTab = TopicLessonsFragment()
        if (storyId.isNotEmpty())
          args.putString(STORY_ID_ARGUMENT_KEY, storyId)
        topicPlayTab.arguments = args
        return topicPlayTab
      }
      TopicTab.PRACTICE -> {
        val topicPracticeTab = TopicPracticeFragment()
        topicPracticeTab.arguments = args
        return topicPracticeTab
      }
      TopicTab.REVIEW -> {
        val topicReviewTab = TopicReviewFragment()
        topicReviewTab.arguments = args
        return topicReviewTab
      }
    }
  }

  override fun getCount(): Int {
    return TopicTab.values().size
  }
}

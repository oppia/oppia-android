package org.oppia.app.topic

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.oppia.app.topic.overview.TopicOverviewFragment
import org.oppia.app.topic.play.TopicPlayFragment
import org.oppia.app.topic.review.TopicReviewFragment
import org.oppia.app.topic.train.TopicTrainFragment

/** Adapter to bind fragments to [FragmentStatePagerAdapter] inside [TopicFragment]. */
class ViewPagerAdapter(fragmentManager: FragmentManager, private val topicId: String) :
  FragmentStatePagerAdapter(fragmentManager) {

  override fun getItem(position: Int): Fragment? {
    val args = Bundle()
    args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
    when (TopicTab.getTabForPosition(position)) {
      TopicTab.OVERVIEW -> {
        val topicOverviewTab = TopicOverviewFragment()
        topicOverviewTab.arguments = args
        return topicOverviewTab
      }
      TopicTab.PLAY -> {
        val topicPlayTab = TopicPlayFragment()
        topicPlayTab.arguments = args
        return topicPlayTab
      }
      TopicTab.TRAIN -> {
        val topicTrainTab = TopicTrainFragment()
        topicTrainTab.arguments = args
        return topicTrainTab
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


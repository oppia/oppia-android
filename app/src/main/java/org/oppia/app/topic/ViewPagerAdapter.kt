package org.oppia.app.topic

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.oppia.app.topic.overview.TopicOverviewFragment
import org.oppia.app.topic.play.TopicPlayFragment
import org.oppia.app.topic.review.TopicReviewFragment
import org.oppia.app.topic.train.TopicTrainFragment

class ViewPagerAdapter(
  fm: FragmentManager,
  private val numOfTabs: Int,
  private val topicId: String,
  private val  topicFragmentPresenter: TopicFragmentPresenter
) :
  FragmentStatePagerAdapter(fm) {

  override fun getItem(position: Int): Fragment? {
    val args = Bundle()
    args.putString(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
    when (position) {
      0 -> {
        val topicOverviewTab = TopicOverviewFragment(topicFragmentPresenter)
        topicOverviewTab.arguments = args
        return topicOverviewTab
      }
      1 -> {
        val topicPlayTab = TopicPlayFragment()
        topicPlayTab.arguments = args
        return topicPlayTab
      }
      2 -> {
        val topicTrainTab = TopicTrainFragment()
        topicTrainTab.arguments = args
        return topicTrainTab
      }
      3 -> {
        val topicReviewTab = TopicReviewFragment()
        topicReviewTab.arguments = args
        return topicReviewTab
      }
      else -> return null
    }
  }

  override fun getCount(): Int {
    return numOfTabs
  }
}


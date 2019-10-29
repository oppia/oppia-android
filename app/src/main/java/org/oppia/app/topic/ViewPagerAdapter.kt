package org.oppia.app.topic

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.oppia.app.topic.overview.TopicOverviewFragment
import org.oppia.app.topic.play.TopicPlayFragment
import org.oppia.app.topic.review.TopicReviewFragment
import org.oppia.app.topic.train.TopicTrainFragment

class ViewPagerAdapter(fm: FragmentManager, private val numOfTabs: Int, private val topicId: String) :
  FragmentStatePagerAdapter(fm) {

  override fun getItem(position: Int): Fragment? {
    val args = Bundle()
    args.putString(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
    when (position) {
      0 -> {
        val tab1 = TopicOverviewFragment()
        tab1.arguments = args
        return tab1
      }
      1 -> {
        val tab2 = TopicPlayFragment()
        tab2.arguments = args
        return tab2
      }
      2 -> {
        val tab3 = TopicTrainFragment()
        tab3.arguments = args
        return tab3
      }
      3 -> {
        val tab4 = TopicReviewFragment()
        tab4.arguments = args
        return tab4
      }
      else -> return null
    }
  }

  override fun getCount(): Int {
    return numOfTabs
  }
}


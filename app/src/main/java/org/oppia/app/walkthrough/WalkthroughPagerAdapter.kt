package org.oppia.app.walkthrough

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.oppia.app.walkthrough.end.WalkthroughFinalFragment
import org.oppia.app.walkthrough.topiclist.WalkthroughTopicListFragment
import org.oppia.app.walkthrough.welcome.WalkthroughWelcomeFragment

class WalkthroughPagerAdapter(fragmentManager: FragmentManager) :
  FragmentStatePagerAdapter(fragmentManager) {

  override fun getItem(position: Int): Fragment = when (position) {
    0 -> WalkthroughWelcomeFragment()
    1 -> WalkthroughTopicListFragment()
    2 -> WalkthroughFinalFragment()
    else -> WalkthroughWelcomeFragment()
  }

  override fun getCount() = 3
}

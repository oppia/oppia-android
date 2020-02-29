package org.oppia.app.walkthrough

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.oppia.app.walkthrough.welcome.WalkthroughWelcomeFragment

class WalkthroughPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

  override fun getItemCount(): Int = 3

  override fun createFragment(position: Int): Fragment = WalkthroughWelcomeFragment()

}

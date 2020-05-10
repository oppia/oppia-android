package org.oppia.app.mydownloads

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/** Adapter to bind fragments to [FragmentStatePagerAdapter] inside [MyDownloadsFragment]. */
@Suppress("DEPRECATION")
class MyDownloadsViewPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

  override fun getItem(position: Int): Fragment {
    return when (MyDownloadsTab.getTabForPosition(position)) {
      MyDownloadsTab.DOWNLOADS -> {
        DownloadsTabFragment()
      }
      MyDownloadsTab.UPDATES -> {
        UpdatesTabFragment()
      }
    }
  }

  override fun getCount(): Int {
    return MyDownloadsTab.values().size
  }
}

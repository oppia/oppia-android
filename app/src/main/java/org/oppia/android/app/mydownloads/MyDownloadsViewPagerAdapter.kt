package org.oppia.android.app.mydownloads

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/** Adapter to bind fragments to [FragmentStateAdapter] inside [MyDownloadsFragment]. */
@Suppress("DEPRECATION")
class MyDownloadsViewPagerAdapter(fragment: Fragment) :
  FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int {
    return MyDownloadsTab.values().size
  }

  override fun createFragment(position: Int): Fragment {
    return when (MyDownloadsTab.getTabForPosition(position)) {
      MyDownloadsTab.DOWNLOADS -> {
        DownloadsTabFragment()
      }
      MyDownloadsTab.UPDATES -> {
        UpdatesTabFragment()
      }
    }
  }
}

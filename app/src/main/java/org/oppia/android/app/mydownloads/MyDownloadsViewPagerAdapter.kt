package org.oppia.android.app.mydownloads

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.oppia.android.app.mydownloads.downloads.DownloadsFragment
import org.oppia.android.app.mydownloads.updates.UpdatesFragment

/** Adapter to bind fragments to [FragmentStateAdapter] inside [MyDownloadsActivity]. */
class MyDownloadsViewPagerAdapter(
  activity: AppCompatActivity,
  val internalProfileId: Int
) : FragmentStateAdapter(activity) {

  override fun getItemCount(): Int {
    return MyDownloadsTab.values().size
  }

  override fun createFragment(position: Int): Fragment {
    return when (MyDownloadsTab.getTabForPosition(position)) {
      MyDownloadsTab.DOWNLOADS -> {
        DownloadsFragment.newInstance(internalProfileId)
      }
      MyDownloadsTab.UPDATES -> {
        UpdatesFragment()
      }
    }
  }
}

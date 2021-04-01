package org.oppia.android.app.mydownloads

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.oppia.android.app.mydownloads.downloads.DownloadsFragment
import org.oppia.android.app.mydownloads.updates.UpdatesFragment
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY

/** Adapter to bind fragments to [FragmentStateAdapter] inside [MyDownloadsFragment]. */
class MyDownloadsViewPagerAdapter(
  fragment: Fragment,
  val internalProfileId: Int
) :
  FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int {
    return MyDownloadsTab.values().size
  }

  override fun createFragment(position: Int): Fragment {
    return when (MyDownloadsTab.getTabForPosition(position)) {
      MyDownloadsTab.DOWNLOADS -> {
        val downloadsFragment = DownloadsFragment()
        val args = Bundle()
        args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
        downloadsFragment.arguments = args
        downloadsFragment
      }
      MyDownloadsTab.UPDATES -> {
        UpdatesFragment()
      }
    }
  }
}

package org.oppia.android.app.mydownloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.databinding.MyDownloadsFragmentBinding
import javax.inject.Inject

/** The presenter for [MyDownloadsFragment]. */
@FragmentScope
class MyDownloadsFragmentPresenter @Inject constructor(private val fragment: Fragment) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = MyDownloadsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment
    val tabLayout = binding
      .root
      .findViewById(
        R.id.my_downloads_tabs_container
      ) as TabLayout
    val viewPager = binding
      .root
      .findViewById(
        R.id.my_downloads_tabs_viewpager
      ) as ViewPager2
    setUpViewPager(tabLayout, viewPager)
    return binding.root
  }

  private fun setUpViewPager(tabLayout: TabLayout, viewPager2: ViewPager2) {
    val adapter = MyDownloadsViewPagerAdapter(fragment)
    viewPager2.adapter = adapter

    TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
      when (position) {
        0 -> tab.text = fragment.getString(R.string.tab_downloads)
        1 -> tab.text = fragment.getString(R.string.tab_updates)
      }
    }.attach()
  }
}

package org.oppia.android.app.mydownloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import org.oppia.android.app.R
import org.oppia.android.app.databinding.MyDownloadsFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
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
      ) as ViewPager
    setUpViewPager(tabLayout, viewPager)
    return binding.root
  }

  private fun setUpViewPager(tabLayout: TabLayout, viewPager: ViewPager) {
    val adapter = MyDownloadsViewPagerAdapter(fragment.childFragmentManager)
    viewPager.adapter = adapter
    tabLayout.setupWithViewPager(viewPager)
    tabLayout.getTabAt(0)!!.text = fragment.getString(R.string.tab_downloads)
    tabLayout.getTabAt(1)!!.text = fragment.getString(R.string.tab_updates)
  }
}

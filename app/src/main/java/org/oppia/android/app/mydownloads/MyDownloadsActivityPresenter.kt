package org.oppia.android.app.mydownloads

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.databinding.MyDownloadsActivityBinding
import javax.inject.Inject

const val INTERNAL_PROFILE_ID_SAVED_KEY = "MyDownloadsActivity.internal_profile_id"
const val IS_ALLOWED_DOWNLOAD_ACCESS_SAVED_KEY = "MyDownloadsActivity.is_allowed_download_access"

/** The presenter for [MyDownloadsActivity]. */
@ActivityScope
class MyDownloadsActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private lateinit var toolbar: Toolbar
  private var internalProfileId: Int = -1

  fun handleOnCreate(internalProfileId: Int, isFromNavigationDrawer: Boolean) {
    val binding =
      DataBindingUtil.setContentView<MyDownloadsActivityBinding>(
        activity,
        R.layout.my_downloads_activity
      )
    binding.apply {
      lifecycleOwner = activity
    }
    toolbar = binding.myDownloadsActivityToolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.title = activity.getString(R.string.menu_my_downloads)

    this.internalProfileId = internalProfileId
    if (isFromNavigationDrawer) {
      setUpNavigationDrawer()
    } else {
      activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    val tabLayout = binding.myDownloadsTabsContainer
    val viewPager = binding.myDownloadsTabsViewpager
    setUpViewPager(tabLayout, viewPager)
  }

  private fun setUpViewPager(tabLayout: TabLayout, viewPager2: ViewPager2) {
    val adapter = MyDownloadsViewPagerAdapter(activity, internalProfileId)
    viewPager2.adapter = adapter

    TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
      val myDownloadsTab = MyDownloadsTab.getTabForPosition(position)
      tab.text = activity.getString(myDownloadsTab.tabLabelResId)
    }.attach()
  }

  private fun setUpNavigationDrawer() {
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.my_downloads_activity_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      activity.findViewById<View>(R.id.my_downloads_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_my_downloads
    )
  }
}

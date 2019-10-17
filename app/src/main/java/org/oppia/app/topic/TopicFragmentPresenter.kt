package org.oppia.app.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

import org.oppia.app.R
import org.oppia.app.databinding.TopicFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.topic.overview.TopicOverviewFragment
import org.oppia.app.topic.play.TopicPlayFragment
import org.oppia.app.topic.review.TopicReviewFragment
import org.oppia.app.topic.train.TopicTrainFragment
import javax.inject.Inject

/** The controller for [TopicFragment]. */
@FragmentScope
class TopicFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var tabLayout: TabLayout
  private lateinit var viewPager: ViewPager

  private val tabIcons =
    intArrayOf(
      R.drawable.ic_overview_white_24dp,
      R.drawable.ic_overview_white_24dp,
      R.drawable.ic_overview_white_24dp,
      R.drawable.ic_overview_white_24dp
    )

  private fun setupTabIcons() {
    tabLayout.getTabAt(0)!!.setIcon(tabIcons[0])
    tabLayout.getTabAt(1)!!.setIcon(tabIcons[1])
    tabLayout.getTabAt(2)!!.setIcon(tabIcons[2])
    tabLayout.getTabAt(3)!!.setIcon(tabIcons[3])
  }

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.lifecycleOwner = fragment
    viewPager = binding.root.findViewById(R.id.viewpager) as ViewPager
    setupViewPager(viewPager)
    tabLayout = binding.root.findViewById(R.id.tabs) as TabLayout
    tabLayout.setupWithViewPager(viewPager)
    setupTabIcons()
    return binding.root
  }

  private fun setupViewPager(viewPager: ViewPager) {
    val adapter = ViewPagerAdapter(fragment.fragmentManager!!)
    adapter.addFragment(TopicOverviewFragment(), "OVERVIEW")
    adapter.addFragment(TopicPlayFragment(), "PLAY")
    adapter.addFragment(TopicTrainFragment(), "TRAIN")
    adapter.addFragment(TopicReviewFragment(), "REVIEW")
    viewPager.adapter = adapter
  }

  internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()
    override fun getItem(position: Int): Fragment {
      return mFragmentList[position]
    }

    override fun getCount(): Int {
      return mFragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
      mFragmentList.add(fragment)
      mFragmentTitleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence {
      return mFragmentTitleList[position]
    }
  }
}

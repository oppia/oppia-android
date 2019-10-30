package org.oppia.app.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import org.oppia.app.R
import org.oppia.app.databinding.TopicFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [TopicFragment]. */
@FragmentScope
class TopicFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var tabLayout: TabLayout
  private lateinit var viewPager: ViewPager
  private val tabIcons =
    intArrayOf(
      R.drawable.ic_overview_white_24dp,
      R.drawable.ic_play_icon,
      R.drawable.ic_train_icon,
      R.drawable.ic_review_icon
    )

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    topicId: String?
  ): View? {
    val binding = TopicFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.lifecycleOwner = fragment
    viewPager = binding.root.findViewById(R.id.topic_tabs_viewpager) as ViewPager
    tabLayout = binding.root.findViewById(R.id.topic_tabs_container) as TabLayout
    setUpViewPager(viewPager, topicId)
    return binding.root
  }
   fun setCurrentTab(tabNumber: Int){
     viewPager.setCurrentItem(tabNumber,true)
   }
  private fun setUpViewPager(viewPager: ViewPager, topicId: String?) {
    val adapter = ViewPagerAdapter(fragment.fragmentManager!!, tabIcons.size, topicId!!,this)
    viewPager.adapter = adapter
    tabLayout.setupWithViewPager(viewPager)
    tabLayout.getTabAt(0)!!.setText(fragment.getString(R.string.overview)).setIcon(tabIcons[0])
    tabLayout.getTabAt(1)!!.setText(fragment.getString(R.string.play)).setIcon(tabIcons[1])
    tabLayout.getTabAt(2)!!.setText(fragment.getString(R.string.train)).setIcon(tabIcons[2])
    tabLayout.getTabAt(3)!!.setText(fragment.getString(R.string.review)).setIcon(tabIcons[3])
  }
}

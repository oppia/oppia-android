package org.oppia.app.walkthrough

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import org.oppia.app.R
import org.oppia.app.databinding.WalkthroughFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [WalkthroughFragment]. */
@FragmentScope
class WalkthroughFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var binding: WalkthroughFragmentBinding
  private var currentProgress: Int = 0
  private lateinit var viewPager: ViewPager

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = WalkthroughFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      viewPager = binding.root.findViewById(R.id.walkthrough_view_pager) as ViewPager
    }
    setUpViewPager(viewPager)

    return binding.root
  }

  private fun setUpViewPager(viewPager: ViewPager) {
    val adapter = WalkthroughPagerAdapter(fragment.childFragmentManager)
    viewPager.apply {
      this.adapter = adapter

    }

    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrollStateChanged(state: Int) {}

      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

      override fun onPageSelected(position: Int) {
        currentProgress = position + 1
        binding.walkthroughProgressBar.progress = currentProgress
      }
    })
  }

  fun prevPage() {
    if (currentProgress > 0) {
      binding.walkthroughProgressBar.progress = --currentProgress
    }
  }
}

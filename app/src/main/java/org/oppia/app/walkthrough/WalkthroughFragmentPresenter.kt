package org.oppia.app.walkthrough

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
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
  private lateinit var viewPager: ViewPager2

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = WalkthroughFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      viewPager = binding.root.findViewById(R.id.walkthrough_view_pager) as ViewPager2

    }
    setUpViewPager(viewPager)


    return binding.root
  }

  private fun setUpViewPager(viewPager: ViewPager2) {
    val adapter = WalkthroughPagerAdapter(fragment.requireActivity())
    viewPager.apply {
      this.adapter = adapter
      isUserInputEnabled = false

    }
    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        binding.walkthroughProgressBar.progress = position
      }

    })
  }

  fun nextPage() {
    if (currentProgress < 3) {
      binding.walkthroughProgressBar.progress = ++currentProgress
    }
  }

  fun prevPage() {
    if (currentProgress > 0) {
      binding.walkthroughProgressBar.progress = --currentProgress
    }
  }
}

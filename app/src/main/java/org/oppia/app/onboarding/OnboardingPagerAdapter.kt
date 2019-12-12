package org.oppia.app.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import org.oppia.app.databinding.OnboardingSlideBinding

class OnboardingPagerAdapter(val context: Context, val index: Int) : PagerAdapter() {
  private lateinit var binding: OnboardingSlideBinding

  private lateinit var onboardingViewModel: OnboardingViewModel
  private lateinit var container: ViewGroup

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    this.container = container
    binding = OnboardingSlideBinding.inflate(LayoutInflater.from(context), container, false)

    slideChanged(index)
    return binding.root
  }

  fun slideChanged(index: Int) {
    if (container.childCount > 0) {
      container.removeView(binding.root)
    }
    onboardingViewModel = OnboardingViewModel(context, index)
    binding.viewModel = onboardingViewModel
    container.addView(binding.root)
  }

  override fun getCount(): Int {
    return 4
  }

  override fun isViewFromObject(view: View, `object`: Any): Boolean {
    return view == `object`
  }

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    container.removeView(`object` as ConstraintLayout)
  }
}
package org.oppia.app.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import org.oppia.app.databinding.OnboardingSlideBinding

/** Adapter to control the slide details in onboarding flow. */
class OnboardingPagerAdapter(val context: Context) : PagerAdapter() {
  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val binding = OnboardingSlideBinding.inflate(LayoutInflater.from(context), container, false)
    val onboardingSlideViewModel = OnboardingSlideViewModel(context, ViewPagerSlide.getSlideForPosition(position))
    binding.viewModel = onboardingSlideViewModel
    container.addView(binding.root)
    return binding.root
  }

  override fun getCount(): Int {
    return TOTAL_NUMBER_OF_SLIDES
  }

  override fun isViewFromObject(view: View, `object`: Any): Boolean {
    return view == `object`
  }

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    container.removeView(`object` as ConstraintLayout)
  }
}

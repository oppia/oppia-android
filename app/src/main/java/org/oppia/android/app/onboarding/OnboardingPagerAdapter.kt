package org.oppia.android.app.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.PagerAdapter
import org.oppia.android.databinding.OnboardingSlideBinding
import org.oppia.android.databinding.OnboardingSlideFinalBinding
import java.util.*

/** Adapter to control the slide details in onboarding flow. */
class OnboardingPagerAdapter(
  val context: Context,
  val onboardingSlideFinalViewModel: OnboardingSlideFinalViewModel
) : PagerAdapter() {
  val isRTL = TextUtilsCompat
    .getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL
  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    if (position == TOTAL_NUMBER_OF_SLIDES - 1) {
      val binding =
        OnboardingSlideFinalBinding.inflate(
          LayoutInflater.from(context),
          container,
          false
        )
      binding.viewModel = onboardingSlideFinalViewModel
      if (isRTL) {
        binding.finalLayout!!.rotationY = 180f
      }

      container.addView(binding.root)
      return binding.root
    }

    val binding = OnboardingSlideBinding.inflate(
      LayoutInflater.from(context),
      container,
      false
    )
    if (isRTL) {
      binding.root.rotationY = 180f
    }
    val onboardingSlideViewModel =
      OnboardingSlideViewModel(context, ViewPagerSlide.getSlideForPosition(position))
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
    container.removeView(`object` as ScrollView)
  }
}

package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import org.oppia.android.R
import org.oppia.android.databinding.OnboardingFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.util.statusbar.StatusBarColor
import javax.inject.Inject

/** The presenter for [OnboardingFragment]. */
@FragmentScope
class OnboardingFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<OnboardingViewModel>,
  private val viewModelProviderFinalSlide: ViewModelProvider<OnboardingSlideFinalViewModel>
) : OnboardingNavigationListener {
  private val dotsList = ArrayList<ImageView>()
  private lateinit var onboardingPagerAdapter: OnboardingPagerAdapter
  private lateinit var binding: OnboardingFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = OnboardingFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      it.viewModel = getOnboardingViewModel()
    }
    setUpViewPager()
    addDots()
    return binding.root
  }

  private fun setUpViewPager() {
    onboardingPagerAdapter =
      OnboardingPagerAdapter(fragment.requireContext(), getOnboardingSlideFinalViewModel())
    binding.onboardingSlideViewPager.adapter = onboardingPagerAdapter
    binding.onboardingSlideViewPager.addOnPageChangeListener(
      object :
        ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(
          position: Int,
          positionOffset: Float,
          positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
          if (position == TOTAL_NUMBER_OF_SLIDES - 1) {
            binding.onboardingSlideViewPager.currentItem = TOTAL_NUMBER_OF_SLIDES - 1
            getOnboardingViewModel().slideChanged(TOTAL_NUMBER_OF_SLIDES - 1)
          } else {
            getOnboardingViewModel().slideChanged(
              ViewPagerSlide.getSlideForPosition(position)
                .ordinal
            )
          }
          selectDot(position)
          onboardingStatusBarColorUpdate(position)
        }
      })
  }

  private fun onboardingStatusBarColorUpdate(position: Int) {
    when (position) {
      0 -> StatusBarColor.statusBarColorUpdate(
        R.color.onboarding1StatusBar,
        activity,
        false
      )
      1 -> StatusBarColor.statusBarColorUpdate(
        R.color.onboarding2StatusBar,
        activity,
        false
      )
      2 -> StatusBarColor.statusBarColorUpdate(
        R.color.onboarding3StatusBar,
        activity,
        false
      )
      3 -> StatusBarColor.statusBarColorUpdate(
        R.color.onboarding4StatusBar,
        activity,
        false
      )
      else -> StatusBarColor.statusBarColorUpdate(
        R.color.colorPrimaryDark,
        activity,
        false
      )
    }
  }

  override fun clickOnSkip() {
    binding.onboardingSlideViewPager.currentItem = TOTAL_NUMBER_OF_SLIDES - 1
  }

  override fun clickOnNext() {
    val position: Int = binding.onboardingSlideViewPager.currentItem + 1
    binding.onboardingSlideViewPager.currentItem = position
    if (position != TOTAL_NUMBER_OF_SLIDES - 1) {
      getOnboardingViewModel().slideChanged(ViewPagerSlide.getSlideForPosition(position).ordinal)
    } else {
      getOnboardingViewModel().slideChanged(TOTAL_NUMBER_OF_SLIDES - 1)
    }
    selectDot(position)
  }

  private fun getOnboardingViewModel(): OnboardingViewModel {
    return viewModelProvider.getForFragment(fragment, OnboardingViewModel::class.java)
  }

  private fun getOnboardingSlideFinalViewModel(): OnboardingSlideFinalViewModel {
    return viewModelProviderFinalSlide.getForFragment(
      fragment,
      OnboardingSlideFinalViewModel::class.java
    )
  }

  private fun addDots() {
    val dotsLayout = binding.slideDotsContainer
    val dotIdList = ArrayList<Int>()
    dotIdList.add(R.id.onboarding_dot_0)
    dotIdList.add(R.id.onboarding_dot_1)
    dotIdList.add(R.id.onboarding_dot_2)
    dotIdList.add(R.id.onboarding_dot_3)
    for (index in 0 until TOTAL_NUMBER_OF_SLIDES) {
      val dotView = ImageView(activity)
      dotView.id = dotIdList[index]
      dotView.setImageResource(R.drawable.onboarding_dot_active)

      val params = LinearLayout.LayoutParams(
        activity.resources.getDimensionPixelSize(R.dimen.dot_width_height),
        activity.resources.getDimensionPixelSize(R.dimen.dot_width_height)
      )
      params.setMargins(
        activity.resources.getDimensionPixelSize(R.dimen.dot_gap),
        0,
        0,
        0
      )
      dotsLayout.addView(dotView, params)
      dotsList.add(dotView)
    }
    selectDot(0)
  }

  private fun selectDot(position: Int) {
    for (index in 0 until TOTAL_NUMBER_OF_SLIDES) {
      val alphaValue = if (index == position) 1.0F else 0.3F
      dotsList[index].alpha = alphaValue
    }
  }
}

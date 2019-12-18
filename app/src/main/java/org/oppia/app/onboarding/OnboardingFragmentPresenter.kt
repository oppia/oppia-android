package org.oppia.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.onboarding_fragment.view.*
import org.oppia.app.R
import org.oppia.app.databinding.OnboardingFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [OnboardingFragment]. */
@FragmentScope
class OnboardingFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<OnboardingViewModel>
) {
  private val dotsList = ArrayList<ImageView>()
  private lateinit var onboardingPagerAdapter: OnboardingPagerAdapter
  private val routeToProfileListener = activity as RouteToProfileListener
  private lateinit var binding: OnboardingFragmentBinding
  private lateinit var slidesViewPager: ViewPager
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = OnboardingFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      it.viewModel = getOnboardingViewModel()
    }
    slidesViewPager = binding.root.onboarding_slide_view_pager as ViewPager
    setUpViewPager(slidesViewPager)
    addDots()
    return binding.root
  }

  private fun setUpViewPager(viewPager: ViewPager) {
    onboardingPagerAdapter = OnboardingPagerAdapter(fragment.requireContext())
    viewPager.adapter = onboardingPagerAdapter
    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrollStateChanged(state: Int) {
      }

      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) {
        getOnboardingViewModel().slideChanged(position)
        selectDot(position)
      }
    })
  }

  fun clickOnGetStarted() {
    routeToProfileListener.routeToProfile()
  }

  fun clickOnSkip() {
    getOnboardingViewModel().slideChanged(3)
    slidesViewPager.currentItem = 3
  }

  private fun getOnboardingViewModel(): OnboardingViewModel {
    return viewModelProvider.getForFragment(fragment, OnboardingViewModel::class.java)
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
      if (index != 0) {
        dotView.alpha = 0.3F
      } else {
        dotView.alpha = 1F
      }

      val params = LinearLayout.LayoutParams(
        activity.resources.getDimensionPixelSize(R.dimen.dot_width_height),
        activity.resources.getDimensionPixelSize(R.dimen.dot_width_height)
      )
      params.setMargins(
        activity.resources.getDimensionPixelSize(R.dimen.dot_gap),
        0,
        activity.resources.getDimensionPixelSize(R.dimen.dot_gap),
        0
      )
      dotsLayout.addView(dotView, params)

      dotsList.add(dotView)
    }
  }

  private fun selectDot(position: Int) {
    for (index in 0 until TOTAL_NUMBER_OF_SLIDES) {
      val alphaValue = if (index == position) 1.0F else 0.3F
      dotsList[index].alpha = alphaValue
    }
  }
}

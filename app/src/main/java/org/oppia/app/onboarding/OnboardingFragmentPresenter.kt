package org.oppia.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.onboarding_fragment.view.*
import org.oppia.app.databinding.OnboardingFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [OnboardingFragment]. */
@FragmentScope
class OnboardingFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<OnboardingViewModel>
) {
  private lateinit var binding: OnboardingFragmentBinding
  private lateinit var slidesViewPager: ViewPager
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = OnboardingFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.

    slidesViewPager = binding.root.onboarding_slide_view_pager as ViewPager
    binding.viewModel = getOnboardingViewModel()
    setUpViewPager(slidesViewPager, /* slideNumber= */ 0)
    return binding.root
  }

  private fun setUpViewPager(viewPager: ViewPager, slideNumber: Int) {
    val customPagerAdapter = OnboardingPagerAdapter(fragment.requireContext(), slideNumber)
    viewPager.adapter = customPagerAdapter

    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrollStateChanged(state: Int) {
      }

      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) {
        getOnboardingViewModel().slideChanged(position)
        customPagerAdapter.slideChanged(position)
      }
    })
  }

  private fun getOnboardingViewModel(): OnboardingViewModel {
    return viewModelProvider.getForFragment(fragment, OnboardingViewModel::class.java)
  }
}

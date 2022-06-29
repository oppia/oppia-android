package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.takusemba.spotlight.Spotlight
import java.util.*
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SpotlightViewState
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.spotlight.OverlayPositionAutomator
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.OnboardingFragmentBinding
import org.oppia.android.databinding.OnboardingSlideBinding
import org.oppia.android.databinding.OnboardingSlideFinalBinding
import org.oppia.android.databinding.OverlayBinding
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.statusbar.StatusBarColor

/** The presenter for [OnboardingFragment]. */
@FragmentScope
class OnboardingFragmentPresenter @Inject constructor(
  private val spotlightStateController: SpotlightStateController,
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<OnboardingViewModel>,
  private val viewModelProviderFinalSlide: ViewModelProvider<OnboardingSlideFinalViewModel>,
  private val resourceHandler: AppLanguageResourceHandler
) : OnboardingNavigationListener, SpotlightNavigationListener {
  private val dotsList = ArrayList<ImageView>()
  private lateinit var binding: OnboardingFragmentBinding
  private lateinit var overlayBinding: OverlayBinding
  private lateinit var spotlight: Spotlight

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
//    overlayBinding = OverlayBinding.inflate(inflater, container, false)
//    overlayBinding.let {
//      it.lifecycleOwner = fragment
//      it.presenter = this
//    }
    setUpViewPager()
    addDots()

    return binding.root
  }

  private fun setUpViewPager() {
    val onboardingViewPagerBindableAdapter = createViewPagerAdapter()
    onboardingViewPagerBindableAdapter.setData(
      listOf(
        OnboardingSlideViewModel(
          context = activity, viewPagerSlide = ViewPagerSlide.SLIDE_0, resourceHandler
        ),
        OnboardingSlideViewModel(
          context = activity, viewPagerSlide = ViewPagerSlide.SLIDE_1, resourceHandler
        ),
        OnboardingSlideViewModel(
          context = activity, viewPagerSlide = ViewPagerSlide.SLIDE_2, resourceHandler
        ),
        getOnboardingSlideFinalViewModel()
      )
    )
    binding.onboardingSlideViewPager.adapter = onboardingViewPagerBindableAdapter
    binding.onboardingSlideViewPager.registerOnPageChangeCallback(
      object : ViewPager2.OnPageChangeCallback() {
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

  private fun createViewPagerAdapter(): BindableAdapter<OnboardingViewPagerViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<OnboardingViewPagerViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is OnboardingSlideViewModel -> ViewType.ONBOARDING_MIDDLE_SLIDE
          is OnboardingSlideFinalViewModel -> ViewType.ONBOARDING_FINAL_SLIDE
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.ONBOARDING_MIDDLE_SLIDE,
        inflateDataBinding = OnboardingSlideBinding::inflate,
        setViewModel = OnboardingSlideBinding::setViewModel,
        transformViewModel = { it as OnboardingSlideViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.ONBOARDING_FINAL_SLIDE,
        inflateDataBinding = OnboardingSlideFinalBinding::inflate,
        setViewModel = OnboardingSlideFinalBinding::setViewModel,
        transformViewModel = { it as OnboardingSlideFinalViewModel }
      )
      .build()
  }

  private fun getOnboardingSlideFinalViewModel(): OnboardingSlideFinalViewModel {
    return viewModelProviderFinalSlide.getForFragment(
      fragment,
      OnboardingSlideFinalViewModel::class.java
    )
  }

  private enum class ViewType {
    ONBOARDING_MIDDLE_SLIDE,
    ONBOARDING_FINAL_SLIDE
  }

  private fun onboardingStatusBarColorUpdate(position: Int) {
    when (position) {
      0 -> StatusBarColor.statusBarColorUpdate(
        R.color.onboarding_1_status_bar,
        activity,
        false
      )
      1 -> StatusBarColor.statusBarColorUpdate(
        R.color.onboarding_2_status_bar,
        activity,
        false
      )
      2 -> StatusBarColor.statusBarColorUpdate(
        R.color.onboarding_3_status_bar,
        activity,
        false
      )
      3 -> StatusBarColor.statusBarColorUpdate(
        R.color.onboarding_4_status_bar,
        activity,
        false
      )
      else -> StatusBarColor.statusBarColorUpdate(
        R.color.oppia_primary_dark,
        activity,
        false
      )
    }
  }

  override fun clickOnSkip() {
    binding.onboardingSlideViewPager.currentItem = TOTAL_NUMBER_OF_SLIDES - 1
  }

  override fun clickOnDismiss() {
    spotlight.finish()
  }

  override fun clickOnNextTip() {
    // use this interface to start the next tip

    spotlight.next()
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

  fun computeLastSpotlightCheckpoint() {
    val profileId = ProfileId.newBuilder()
      .setInternalId(123)
      .build()
    val onboardingButtonSpotlightViewStateLiveData =
      spotlightStateController.retrieveSpotlightViewState(
        profileId,
        org.oppia.android.app.model.Spotlight.FeatureCase.ONBOARDING_NEXT_BUTTON
      ).toLiveData()

    onboardingButtonSpotlightViewStateLiveData.observe(
      fragment,
      object : Observer<AsyncResult<Any>> {
        override fun onChanged(it: AsyncResult<Any>?) {
          if (it is AsyncResult.Success) {
            onboardingButtonSpotlightViewStateLiveData.removeObserver(this)
            val viewState = (it.value as SpotlightViewState)
            if (viewState == SpotlightViewState.SPOTLIGHT_SEEN) {
              return
            } else if (viewState == SpotlightViewState.SPOTLIGHT_VIEW_STATE_UNSPECIFIED || viewState == SpotlightViewState.SPOTLIGHT_NOT_SEEN) {
              val overlayPositionAutomator = OverlayPositionAutomator(activity, fragment)
              overlayPositionAutomator.createTarget(
                binding.onboardingFragmentNextImageView,
                "Next",
                OverlayPositionAutomator.Companion.SpotlightShape.Circle
              )
              spotlightStateController.recordSpotlightCheckpoint(
                profileId, org.oppia.android.app.model.Spotlight.FeatureCase.ONBOARDING_NEXT_BUTTON,
                SpotlightViewState.SPOTLIGHT_SEEN
              )
              overlayPositionAutomator.startSpotlight()
            }
          }
        }
      }
    )
  }
}

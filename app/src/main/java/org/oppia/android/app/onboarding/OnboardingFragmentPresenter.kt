package org.oppia.android.app.onboarding

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.policies.RouteToPrivacyPolicyListener
import org.oppia.android.app.policies.RouteToTermsOfServiceListener
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.OnboardingFragmentBinding
import org.oppia.android.databinding.OnboardingSlideBinding
import org.oppia.android.databinding.OnboardingSlideFinalBinding
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.statusbar.StatusBarColor
import javax.inject.Inject

/** The presenter for [OnboardingFragment]. */
@FragmentScope
class OnboardingFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<OnboardingViewModel>,
  private val viewModelProviderFinalSlide: ViewModelProvider<OnboardingSlideFinalViewModel>,
  private val resourceHandler: AppLanguageResourceHandler
) : OnboardingNavigationListener {
  private val dotsList = ArrayList<ImageView>()
  private lateinit var binding: OnboardingFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
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
        setViewModel = this::bindOnboardingSlideFinal,
        transformViewModel = { it as OnboardingSlideFinalViewModel }
      )
      .build()
  }

  private fun bindOnboardingSlideFinal(
    binding: OnboardingSlideFinalBinding,
    model: OnboardingSlideFinalViewModel
  ) {
    binding.viewModel = model
    setTermsAndCondition(binding)
  }

  private fun setTermsAndCondition(binding: OnboardingSlideFinalBinding) {
    val termsOfService: String = resourceHandler.getStringInLocale(R.string.terms_of_service)
    val privacyPolicy: String = resourceHandler.getStringInLocale(R.string.privacy_policy)
    val completeString: String =
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.agree_to_terms,
        resourceHandler.getStringInLocale(R.string.app_name)
      )

    val startIndex = completeString.indexOf(termsOfService)
    val endIndex = startIndex + termsOfService.length
    val startIndex2 = completeString.indexOf(privacyPolicy)
    val endIndex2 = startIndex2 + privacyPolicy.length
    val spannableStringBuilder = SpannableStringBuilder(completeString)
    val clickOnTerms = object : ClickableSpan() {
      override fun onClick(widget: View) {
        (activity as RouteToTermsOfServiceListener).onRouteToTermsOfService()
      }

      override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = ContextCompat.getColor(activity, R.color.oppiaPrimaryTextDark)
        ds.typeface = Typeface.create(ds.typeface, Typeface.BOLD)
      }
    }
    val clickOnPrivacy = object : ClickableSpan() {
      override fun onClick(widget: View) {
        (activity as RouteToPrivacyPolicyListener).onRouteToPrivacyPolicy()
      }

      override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = ContextCompat.getColor(activity, R.color.oppiaPrimaryTextDark)
        ds.typeface = Typeface.create(ds.typeface, Typeface.BOLD)
      }
    }

    if (startIndex > 0)
      spannableStringBuilder.setSpan(
        clickOnTerms,
        startIndex,
        endIndex,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    if (startIndex2 > 0)
      spannableStringBuilder.setSpan(
        clickOnPrivacy,
        startIndex2,
        endIndex2,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    binding.slideTermsOfServiceAndPrivacyPolicyLinksTextView?.text = spannableStringBuilder
    binding.slideTermsOfServiceAndPrivacyPolicyLinksTextView?.movementMethod =
      LinkMovementMethod.getInstance()
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

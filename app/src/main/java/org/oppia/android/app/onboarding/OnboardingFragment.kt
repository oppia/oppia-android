package org.oppia.android.app.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject
import org.oppia.android.R

/** Fragment that contains an onboarding flow of the app. */
class OnboardingFragment : InjectableFragment() {
  @Inject
  lateinit var onboardingFragmentPresenter: OnboardingFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity!!.titleColor = R.color.color_palette_section_title_divider_color
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return onboardingFragmentPresenter.handleCreateView(inflater, container)
  }
}

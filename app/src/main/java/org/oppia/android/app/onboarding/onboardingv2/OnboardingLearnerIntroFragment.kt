package org.oppia.android.app.onboarding.onboardingv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

/** Fragment that contains the  introduction message for new learners. */
class OnboardingLearnerIntroFragment : InjectableFragment() {
  @Inject
  lateinit var onboardingLearnerIntroFragmentPresenter: OnboardingLearnerIntroFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val profileNickname = arguments!!.getStringFromBundle(PROFILE_NICKNAME_ARGUMENT_KEY)!!
    return onboardingLearnerIntroFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileNickname
    )
  }
}

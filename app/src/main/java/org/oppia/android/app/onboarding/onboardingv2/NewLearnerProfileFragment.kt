package org.oppia.android.app.onboarding.onboardingv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment for displaying a new learner profile creation flow. */
class NewLearnerProfileFragment : InjectableFragment() {
  @Inject
  lateinit var learnerProfileFragmentPresenter: NewLearnerProfileFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return learnerProfileFragmentPresenter.handleCreateView(inflater, container)
  }
}

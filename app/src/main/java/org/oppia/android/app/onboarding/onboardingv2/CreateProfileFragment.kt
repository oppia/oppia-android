package org.oppia.android.app.onboarding.onboardingv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment for displaying a new learner profile creation flow. */
class CreateProfileFragment : InjectableFragment() {
  @Inject
  lateinit var learnerProfileFragmentPresenter: CreateProfileFragmentPresenter

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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    learnerProfileFragmentPresenter.handleOnActivityResult(requestCode, resultCode, data)
  }
}

package org.oppia.android.app.policies

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains Privacy Policy flow of the app. */
class PrivacyPolicyFragment : InjectableFragment() {
  @Inject
  lateinit var privacyPolicyFragmentPresenter: PrivacyPolicyFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return privacyPolicyFragmentPresenter.handleCreateView(inflater, container)
  }
}

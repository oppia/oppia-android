package org.oppia.android.app.devoptions.forcenetworktype

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentComponentImpl

/** Fragment to provide functionality to force the network type of the app. */
class ForceNetworkTypeFragment : InjectableFragment() {
  @Inject
  lateinit var forceNetworkTypeFragmentPresenter: ForceNetworkTypeFragmentPresenter

  companion object {
    /** Returns a new instance of [ForceNetworkTypeFragment]. */
    fun newInstance(): ForceNetworkTypeFragment = ForceNetworkTypeFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return forceNetworkTypeFragmentPresenter.handleCreateView(inflater, container)
  }
}

package org.oppia.android.app.devoptions.forcenetworktype

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment to provide functionality to force network type of the app. */
class ForceNetworkTypeFragment : InjectableFragment() {
  @Inject
  lateinit var forceNetworkTypeFragmentPresenter: ForceNetworkTypeFragmentPresenter

  companion object {
    fun newInstance(): ForceNetworkTypeFragment = ForceNetworkTypeFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return forceNetworkTypeFragmentPresenter.handleCreateView(inflater, container)
  }
}

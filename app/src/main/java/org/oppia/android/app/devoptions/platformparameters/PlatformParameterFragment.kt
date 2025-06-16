package org.oppia.android.app.devoptions.platformparameters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment to provide functionality to view and modify feature flags of the app. */
class PlatformParameterFragment : InjectableFragment() {
  @Inject
  lateinit var PlatformParameterFragmentPresenter: PlatformParameterFragmentPresenter

  companion object {
    /** Returns a new instance of [PlatformParameterFragment]. */
    fun newInstance(): PlatformParameterFragment = PlatformParameterFragment()
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
    return PlatformParameterFragmentPresenter.handleCreateView(inflater, container)
  }
}

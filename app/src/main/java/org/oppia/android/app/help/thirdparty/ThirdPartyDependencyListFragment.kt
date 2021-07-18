package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains FAQ list in the app. */
class ThirdPartyDependencyListFragment : InjectableFragment() {
  @Inject
  lateinit var thirdPartyDependencyListFragmentPresenter: ThirdPartyDependencyListFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return thirdPartyDependencyListFragmentPresenter.handleCreateView(inflater, container)
  }
}

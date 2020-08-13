package org.oppia.app.walkthrough.welcome

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** The first slide for [WalkthroughActivity]. */
class WalkthroughWelcomeFragment : InjectableFragment() {
  @Inject
  lateinit var walkthroughWelcomeFragmentPresenter: WalkthroughWelcomeFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return walkthroughWelcomeFragmentPresenter.handleCreateView(inflater, container)
  }
}

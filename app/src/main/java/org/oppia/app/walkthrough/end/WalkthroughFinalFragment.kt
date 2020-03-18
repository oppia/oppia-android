package org.oppia.app.walkthrough.end

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** The final slide for [WalkthroughActivity]. */
class WalkthroughFinalFragment : InjectableFragment() {
  @Inject lateinit var walkthroughFinalFragmentPresenter: WalkthroughFinalFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return walkthroughFinalFragmentPresenter.handleCreateView(inflater, container)
  }
}

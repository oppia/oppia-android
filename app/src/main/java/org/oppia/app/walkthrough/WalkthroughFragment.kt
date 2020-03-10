package org.oppia.app.walkthrough

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains an walkthrough of the app. */
class WalkthroughFragment : InjectableFragment() {
  @Inject lateinit var walkthroughFragmentPresenter: WalkthroughFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return walkthroughFragmentPresenter.handleCreateView(inflater, container)
  }
}

package org.oppia.app.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains all recently played stories. */
class ContinuePlayingFragment : InjectableFragment() {

  @Inject lateinit var continuePlayingFragmentPresenter: ContinuePlayingFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return continuePlayingFragmentPresenter.handleCreateView(inflater, container)
  }

}

package org.oppia.app.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.databinding.ContinuePlayingFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [ContinuePlayingFragment]. */
@FragmentScope
class ContinuePlayingFragmentPresenter @Inject constructor() {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    return ContinuePlayingFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false).root
  }
}

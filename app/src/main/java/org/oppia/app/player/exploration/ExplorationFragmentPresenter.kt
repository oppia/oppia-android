package org.oppia.app.player.exploration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ExplorationFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The controller for [ExplorationFragment]. */
@FragmentScope
class ExplorationFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    return ExplorationFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false).root
  }
}

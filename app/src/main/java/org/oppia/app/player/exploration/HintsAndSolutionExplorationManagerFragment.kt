package org.oppia.app.player.exploration

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/**
 * ManagerFragment of [ExplorationFragment] that observes data provider that retrieve Exploration State.
 */
class HintsAndSolutionExplorationManagerFragment : InjectableFragment() {
  @Inject
  lateinit var hintsAndSolutionExplorationManagerFragmentPresenter: HintsAndSolutionExplorationManagerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    return hintsAndSolutionExplorationManagerFragmentPresenter.handleCreateView()

  }

}

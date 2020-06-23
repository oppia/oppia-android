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
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to HintsAndSolutionFragment" }
    val id =
      checkNotNull(args.getString(EXPLORATION_ID_ARGUMENT_KEY)) { "Expected id to be passed to HintsAndSolutionFragment" }
    val newAvailableHintIndex =
      checkNotNull(args.getInt(NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY)) { "Expected hint index to be passed to HintsAndSolutionFragment" }
    val allHintsExhausted =
      checkNotNull(args.getBoolean(ALL_HINTS_EXHAUSTED_ARGUMENT_KEY)) { "Expected if hints exhausted to be passed to HintsAndSolutionFragment" }

    return hintsAndSolutionExplorationManagerFragmentPresenter.handleCreateView(
      id,
      newAvailableHintIndex,
      allHintsExhausted
    )
  }

  companion object {

    internal const val EXPLORATION_ID_ARGUMENT_KEY = "EXPLORATION_ID"
    internal const val NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY = "NEW_AVAILABLE_HINT_INDEX"
    internal const val ALL_HINTS_EXHAUSTED_ARGUMENT_KEY = "ALL_HINTS_EXHAUSTED"

    fun newInstance(
      explorationId: String,
      newAvailableHintIndex: Int,
      allHintsExhausted: Boolean
    ): HintsAndSolutionExplorationManagerFragment {
      val explorationManagerFragment =
        HintsAndSolutionExplorationManagerFragment()

      val args = Bundle()
      args.putString(EXPLORATION_ID_ARGUMENT_KEY, explorationId)
      args.putInt(NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY, newAvailableHintIndex)
      args.putBoolean(ALL_HINTS_EXHAUSTED_ARGUMENT_KEY, allHintsExhausted)
      explorationManagerFragment.arguments = args
      return explorationManagerFragment
    }
  }
}

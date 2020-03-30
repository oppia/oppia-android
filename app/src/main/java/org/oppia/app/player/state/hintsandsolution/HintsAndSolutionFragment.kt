package org.oppia.app.player.state.hintsandsolution

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.R
import org.oppia.app.fragment.InjectableDialogFragment
import org.oppia.app.model.State
import javax.inject.Inject

private const val KEY_CURRENT_EXPANDED_LIST_INDEX = "CURRENT_EXPANDED_LIST_INDEX"

/* Fragment that displays a fullscreen dialog for Hints and Solutions */
class HintsAndSolutionFragment : InjectableDialogFragment(), ExpandedHintListIndexListener, RevealSolutionInterface {

  @Inject lateinit var hintsAndSolutionFragmentPresenter: HintsAndSolutionFragmentPresenter

  private lateinit var currentState: State
  private lateinit var explorationId: String
  private var newAvailableHintIndex: Int = -1
  private var allHintsExhausted: Boolean = false

  private var currentExpandedHintListIndex: Int? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenHintDialogStyle)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    if (savedInstanceState != null) {
      currentExpandedHintListIndex = savedInstanceState.getInt(KEY_CURRENT_EXPANDED_LIST_INDEX, -1)
      if (currentExpandedHintListIndex == -1) {
        currentExpandedHintListIndex = null
      }
    }
    return hintsAndSolutionFragmentPresenter.handleCreateView(
      inflater,
      container,
      currentState,
      explorationId,
      currentExpandedHintListIndex,
      newAvailableHintIndex,
      allHintsExhausted,
      this as ExpandedHintListIndexListener
    )
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.FullScreenDialogStyle)
  }

  fun setStateAndExplorationId(
    newState: State,
    explorationId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  ) {
    currentState = newState
    this.explorationId = explorationId
    this.newAvailableHintIndex = newAvailableHintIndex
    this.allHintsExhausted = allHintsExhausted
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (currentExpandedHintListIndex != null) {
      outState.putInt(KEY_CURRENT_EXPANDED_LIST_INDEX, currentExpandedHintListIndex!!)
    }
  }

  override fun onExpandListIconClicked(index: Int?) {
    currentExpandedHintListIndex = index
  }

  override fun revealSolution(saveUserChoice: Boolean) {
    hintsAndSolutionFragmentPresenter.handleRevealSolution(saveUserChoice)
  }
}

package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableInt
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.audio.CellularDataInterface
import javax.inject.Inject

private const val CONTINUE = "Continue"
private const val END_EXPLORATION = "EndExploration"
private const val MULTIPLE_CHOICE_INPUT = "MultipleChoiceInput"
private const val ITEM_SELECT_INPUT = "ItemSelectionInput"
private const val TEXT_INPUT = "TextInput"
private const val FRACTION_INPUT = "FractionInput"
private const val NUMERIC_INPUT = "NumericInput"
private const val NUMERIC_WITH_UNITS = "NumberWithUnits"
private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"

/** Fragment that represents the current state of an exploration. */
class StateFragment : InjectableFragment(), CellularDataInterface {
  @Inject
  lateinit var stateFragmentPresenter: StateFragmentPresenter
  // Control this boolean value from controllers in domain module.
  private var showCellularDataDialog = true

  private lateinit var dummyStateIndex: ArrayList<Int>
  private lateinit var dummyInteractionId: ArrayList<String>

  private val currentStateIndex = ObservableInt(0)
  // Indicates the maximum state-index learner has reached.
  // This variable helps in figuring out whether to show "Next" button or not.
  private val maxLearnerProgressIndex = ObservableInt(0)

  init {
    // TODO(#116): Code to control the value of showCellularDataDialog using AudioController.
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createDummyDataForButtonCheck()
    setCurrentStateIndex(dummyStateIndex.get(0), dummyInteractionId.get(0))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return stateFragmentPresenter.handleCreateView(inflater, container)
  }

  fun dummyButtonClicked() {
    if (showCellularDataDialog) {
      stateFragmentPresenter.setAudioFragmentVisible(false)
      showCellularDataDialogFragment()
    } else {
      stateFragmentPresenter.setAudioFragmentVisible(true)
    }
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = childFragmentManager.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = CellularDataDialogFragment.newInstance()
    dialogFragment.showNow(childFragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }

  override fun enableAudioWhileOnCellular(saveUserChoice: Boolean) {
    stateFragmentPresenter.setAudioFragmentVisible(true)
    // saveUserChoice -> true -> save this preference
    // saveUserChoice -> false -> do not save this preference
  }

  override fun disableAudioWhileOnCellular(saveUserChoice: Boolean) {
    // saveUserChoice -> true -> save this preference
    // saveUserChoice -> false -> do not save this preference
  }

  // TODO(#163): Remove this dummy data in final state-implementation
  private fun createDummyDataForButtonCheck() {
    dummyStateIndex = ArrayList()
    dummyInteractionId = ArrayList()

    dummyStateIndex.add(0)
    dummyInteractionId.add("Continue")
    dummyStateIndex.add(1)
    dummyInteractionId.add("MultipleChoiceInput")
    dummyStateIndex.add(2)
    dummyInteractionId.add("TextInput")
    dummyStateIndex.add(3)
    dummyInteractionId.add("Continue")
    dummyStateIndex.add(4)
    dummyInteractionId.add("ItemSelectionInput")
    dummyStateIndex.add(5)
    dummyInteractionId.add("EndExploration")
  }

  /**
   * This function is responsible controlling left and right button visiblty
   *
   * @param [stateIndex]: Int: This controls the left/previous button based on its value.
   * @param [interactionId]: String: This controls the right button based on the interaction-id
   */
  private fun setCurrentStateIndex(stateIndex: Int, interactionId: String) {
    stateFragmentPresenter.hideAllButtons()
    currentStateIndex.set(stateIndex)

    if (stateIndex > 0) {
      stateFragmentPresenter.setPreviousButtonVisible(true)
    }

    if (maxLearnerProgressIndex.get() > currentStateIndex.get()) {
      stateFragmentPresenter.setNextButtonVisible(true)
    } else {
      maxLearnerProgressIndex.set(stateIndex)
      if (interactionId == CONTINUE) {
        stateFragmentPresenter.setContinueButtonVisible(true)
      } else if (interactionId == MULTIPLE_CHOICE_INPUT || interactionId == ITEM_SELECT_INPUT
        || interactionId == TEXT_INPUT || interactionId == FRACTION_INPUT
        || interactionId == NUMERIC_INPUT || interactionId == NUMERIC_WITH_UNITS
      ) {
        stateFragmentPresenter.setSubmitButtonVisible(true)
      } else if (interactionId == END_EXPLORATION) {
        stateFragmentPresenter.setEndExplorationButtonVisible(true)
      } else {
        stateFragmentPresenter.setSubmitButtonVisible(true)
      }
    }
  }

  // Dummy method to go to next state
  private fun nextState() {
    val nextStateIndex = currentStateIndex.get() + 1
    if (nextStateIndex < dummyStateIndex.size) {
      setCurrentStateIndex(dummyStateIndex[nextStateIndex], dummyInteractionId[nextStateIndex])
    }
  }

  // Dummy method to go to previous state
  private fun previousState() {
    val prevStateIndex = currentStateIndex.get() - 1
    if (prevStateIndex >= 0) {
      setCurrentStateIndex(dummyStateIndex[prevStateIndex], dummyInteractionId[prevStateIndex])
    }
  }

  fun continueButtonClicked() {
    // TODO(#163): Handle continue button click here.
    nextState()
  }

  fun endExplorationButtonClicked() {
    // TODO(#163): Handle end-exploration button click here.
  }

  fun learnAgainButtonClicked() {
    // TODO(#163): Handle learn-again button click here.
  }

  fun nextButtonClicked() {
    // TODO(#163): Handle next button click here.
    nextState()
  }

  fun previousButtonClicked() {
    // TODO(#163): Handle previous button click here.
    previousState()
  }

  fun submitButtonClicked() {
    // TODO(#163): Handle submit button click here.
    nextState()
  }
}

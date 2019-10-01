package org.oppia.app.player.state

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.InteractionObject
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_6
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import java.util.*
import javax.inject.Inject

/** [ViewModel] for StateFragment. */
@FragmentScope
class StateViewModel @Inject constructor(
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
) : ViewModel() {
  var isAudioFragmentVisible = ObservableField<Boolean>(false)
  var isContinueButtonVisible = ObservableField<Boolean>(false)
  var isEndExplorationButtonVisible = ObservableField<Boolean>(false)
  var isLearnAgainButtonVisible = ObservableField<Boolean>(false)
  var isNextButtonVisible = ObservableField<Boolean>(false)
  var isPreviousButtonVisible = ObservableField<Boolean>(false)
  var isSubmitButtonVisible = ObservableField<Boolean>(false)
  var isSubmitButtonActive = ObservableField<Boolean>(false)

  init {
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_6)
  }

  val ephemeralStateLiveData: LiveData<EphemeralState> by lazy { getEphemeralState() }

  private fun getEphemeralState(): LiveData<EphemeralState> {
    return Transformations.map(explorationProgressController.getCurrentState(), ::processEphemeralStateResult)
  }

  private fun processEphemeralStateResult(ephemeralState: AsyncResult<EphemeralState>): EphemeralState {
    if (ephemeralState.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve ephemeral state ${ephemeralState.getErrorOrNull()}")
    }
    return ephemeralState.getOrDefault(EphemeralState.getDefaultInstance())
  }

  private val moveToNextState by lazy {
    explorationProgressController.moveToNextState()
  }

  private val moveToPreviousState by lazy {
    explorationProgressController.moveToPreviousState()
  }

  private fun submitAnswer(answer: InteractionObject) {
    var answerOutcome: LiveData<AsyncResult<AnswerOutcome>> = explorationProgressController.submitAnswer(answer)
  }

  fun hideAllButtons() {
    isContinueButtonVisible.set(false)
    isEndExplorationButtonVisible.set(false)
    isLearnAgainButtonVisible.set(false)
    isNextButtonVisible.set(false)
    isPreviousButtonVisible.set(false)
    isSubmitButtonVisible.set(false)
  }

  fun setAudioFragmentVisible(isVisible: Boolean) {
    isAudioFragmentVisible.set(isVisible)
  }

  fun setContinueButtonVisible(isVisible: Boolean) {
    isContinueButtonVisible.set(isVisible)
  }

  fun setEndExplorationButtonVisible(isVisible: Boolean) {
    isEndExplorationButtonVisible.set(isVisible)
  }

  fun setLearnAgainButtonVisible(isVisible: Boolean) {
    isLearnAgainButtonVisible.set(isVisible)
  }

  fun setNextButtonVisible(isVisible: Boolean) {
    isNextButtonVisible.set(isVisible)
  }

  fun setPreviousButtonVisible(isVisible: Boolean) {
    isPreviousButtonVisible.set(isVisible)
  }

  fun setSubmitButtonVisible(isVisible: Boolean) {
    isSubmitButtonVisible.set(isVisible)
  }

  fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    if (s.isNotEmpty()) {
      isSubmitButtonActive.set(true)
    } else {
      isSubmitButtonActive.set(false)
    }
  }
}

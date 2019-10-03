package org.oppia.app.player.state

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Exploration
import org.oppia.app.model.TopicSummary
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_6
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor(
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
) :
  ViewModel() {
  var isAudioFragmentVisible = ObservableField<Boolean>(false)

  fun setAudioFragmentVisible(isVisible: Boolean) {
    isAudioFragmentVisible.set(isVisible)
  }
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

}

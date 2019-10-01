package org.oppia.app.player.state

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Exploration
import org.oppia.app.model.TopicSummary
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor(private val explorationDataController: ExplorationDataController) :
  ViewModel() {
  var isAudioFragmentVisible = ObservableField<Boolean>(false)

  var explorationID: String=""

  fun setAudioFragmentVisible(isVisible: Boolean) {
    isAudioFragmentVisible.set(isVisible)
  }

  /**
   * The retrieved [LiveData] for retrieving explorations. This model should ensure only one
   * [LiveData] is used for all subsequent processed data to ensure the transformed [LiveData]s are
   * always in sync.
   */
  fun getExplorationById(explorationID: String): LiveData<AsyncResult<Exploration>> {
    this.explorationID = explorationID
    return explorationResultLiveData
  }

  val explorationResultLiveData: LiveData<AsyncResult<Exploration>> by lazy {
    explorationDataController.getExplorationById(explorationID)
  }
}

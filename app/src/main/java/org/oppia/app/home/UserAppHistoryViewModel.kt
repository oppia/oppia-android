package org.oppia.app.home

import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.UserAppHistory
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.util.logging.Logger
import org.oppia.domain.UserAppHistoryController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** [ViewModel] for user app usage history. */
@FragmentScope
class UserAppHistoryViewModel @Inject constructor(
  private val userAppHistoryController: UserAppHistoryController,
  private val logger: Logger,
  private val explorationProgressController: ExplorationProgressController
): ObservableViewModel() {
  val userAppHistoryLiveData: LiveData<UserAppHistory>? by lazy {
    getUserAppHistory()
  }

  val ephemeralStateLiveData: LiveData<EphemeralState> by lazy {
    getEphemeralState()
  }

  @Bindable
  var userProvidedExplorationId: String? = null

  @Bindable
  var currentStateLabel: String? = null
    set(value) {
      field = value
      notifyChange()
    }

  private fun getUserAppHistory(): LiveData<UserAppHistory>? {
    // If there's an error loading the data, assume the default.
    return Transformations.map(userAppHistoryController.getUserAppHistory(), ::processUserAppHistoryResult)
  }

  private fun getEphemeralState(): LiveData<EphemeralState> {
    return Transformations.map(explorationProgressController.getCurrentState(), ::processCurrentState)
  }

  private fun processUserAppHistoryResult(appHistoryResult: AsyncResult<UserAppHistory>): UserAppHistory {
    if (appHistoryResult.isFailure()) {
      logger.e("HomeFragment", "Failed to retrieve user app history", appHistoryResult.getErrorOrNull()!!)
    }
    return appHistoryResult.getOrDefault(UserAppHistory.getDefaultInstance())
  }

  private fun processCurrentState(ephemeralStateResult: AsyncResult<EphemeralState>): EphemeralState {
    if (ephemeralStateResult.isFailure()) {
      logger.e("HomeFragment", "Failed to retrieve ephemeral state", ephemeralStateResult.getErrorOrNull()!!)
      currentStateLabel = "Failed to retrieve state: ${ephemeralStateResult.getErrorOrNull()}"
    }
    return ephemeralStateResult.getOrDefault(EphemeralState.getDefaultInstance())
  }
}

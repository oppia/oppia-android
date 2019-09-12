package org.oppia.app.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.UserAppHistory
import org.oppia.domain.UserAppHistoryController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject
/** [ViewModel] for user app usage history. */
@FragmentScope
class UserAppHistoryViewModel @Inject constructor(
  private val userAppHistoryController: UserAppHistoryController
): ViewModel() {
  val userAppHistoryLiveData: LiveData<UserAppHistory>? by lazy {
    getUserAppHistory()
  }

  private fun getUserAppHistory(): LiveData<UserAppHistory>? {
    // If there's an error loading the data, assume the default.
    return Transformations.map(userAppHistoryController.getUserAppHistory(), ::processUserAppHistoryResult)
  }

  private fun processUserAppHistoryResult(appHistoryResult: AsyncResult<UserAppHistory>): UserAppHistory {
    if (appHistoryResult.isFailure()) {
      Log.e("HomeFragment", "Failed to retrieve user app history", appHistoryResult.getErrorOrNull())
    }
    return appHistoryResult.getOrDefault(UserAppHistory.getDefaultInstance())
  }
}

package org.oppia.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.oppia.app.model.UserAppHistory

/** [ViewModel] for user app usage history. */
class UserAppHistoryViewModel: ViewModel() {
  var userAppHistoryLiveData: LiveData<UserAppHistory>? = null
}

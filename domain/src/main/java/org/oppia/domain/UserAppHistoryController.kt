package org.oppia.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import org.oppia.app.model.UserAppHistory
import org.oppia.util.data.AsyncDataSource
import org.oppia.util.data.AsyncResult

/** Controller for persisting and retrieving the previous user history of using the app. */
class UserAppHistoryController {
  // TODO(BenHenning): Persist this value.
  private var userOpenedApp = false

  private val userAppHistoryData: LiveData<AsyncResult<UserAppHistory>> = liveData {
    emit(AsyncResult.success(createUserAppHistoryDataSource().executePendingOperation()))
  }

  /** Saves that the user has opened the app. */
  fun markUserOpenedApp() {
    userOpenedApp = true
  }

  /** Returns a [LiveData] result indicating whether the user has previously opened the app. */
  fun getUserAppHistory(): LiveData<AsyncResult<UserAppHistory>> {
    return userAppHistoryData
  }

  // TODO(BenHenning): Move this to a data source within the data source module.
  private fun createUserAppHistoryDataSource(): AsyncDataSource<UserAppHistory> {
    return object : AsyncDataSource<UserAppHistory> {
      override suspend fun executePendingOperation(): UserAppHistory {
        return UserAppHistory.newBuilder().setAlreadyOpenedApp(userOpenedApp).build()
      }
    }
  }
}
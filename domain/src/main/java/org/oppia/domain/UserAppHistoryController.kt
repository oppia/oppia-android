package org.oppia.domain

import android.util.Log
import androidx.lifecycle.LiveData
import org.oppia.app.model.UserAppHistory
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for persisting and retrieving the previous user history of using the app. */
@Singleton
class UserAppHistoryController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory, private val dataProviders: DataProviders
) {
  private val appHistoryStore = cacheStoreFactory.create("user_app_history", UserAppHistory.getDefaultInstance())

  init {
    // Prime the cache ahead of time so that any existing history is read prior to any calls to markUserOpenedApp().
    appHistoryStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        Log.e("DOMAIN", "Failed to prime cache ahead of LiveData conversion for user app open history.", it)
      }
    }
  }

  /**
   * Saves that the user has opened the app. Note that this does not notify existing subscribers of the changed state,
   * nor can future subscribers observe this state until app restart.
   */
  fun markUserOpenedApp() {
    appHistoryStore.storeDataAsync(updateInMemoryCache = false) {
      it.toBuilder().setAlreadyOpenedApp(true).build()
    }.invokeOnCompletion {
      it?.let {
        Log.e("DOMAIN", "Failed when storing that the user already opened the app.", it)
      }
    }
  }

  /** Clears any indication that the user has previously opened the application. */
  fun clearUserAppHistory() {
    appHistoryStore.clearCacheAsync().invokeOnCompletion {
      it?.let {
        Log.e("DOMAIN", "Failed to clear user app history.", it)
      }
    }
  }

  /**
   * Returns a [LiveData] result indicating whether the user has previously opened the app. This is guaranteed to
   * provide the state of the store upon the creation of this controller even if [markUserOpenedApp] has since been
   * called.
   */
  fun getUserAppHistory(): LiveData<AsyncResult<UserAppHistory>> {
    return dataProviders.convertToLiveData(appHistoryStore)
  }
}

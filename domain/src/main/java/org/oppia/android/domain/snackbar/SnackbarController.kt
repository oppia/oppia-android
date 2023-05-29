package org.oppia.android.domain.snackbar

import androidx.annotation.StringRes
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton

private const val SNACKBAR_REQUEST_PROVIDER_ID = "snackbar_request_provider_id"

@Singleton
class SnackbarController @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
) {

  private val _snackbarRequestQueue: Queue<SnackbarRequest.ShowSnackbar> = LinkedList()

  val snackbarRequestQueue: Queue<SnackbarRequest.ShowSnackbar>
    get() = _snackbarRequestQueue

  fun getCurrentSnackbar(): DataProvider<SnackbarRequest> {
    val currentRequest = _snackbarRequestQueue.peek()
    return dataProviders.createInMemoryDataProvider(SNACKBAR_REQUEST_PROVIDER_ID) {
      return@createInMemoryDataProvider currentRequest
        ?: SnackbarRequest.ShowNothing
    }
  }

  fun enqueueSnackbar(request: SnackbarRequest.ShowSnackbar) {
    _snackbarRequestQueue.add(request)
    notifyPotentialSnackbarChange()
  }

  fun dismissCurrentSnackbar() {
    _snackbarRequestQueue.remove()
    notifyPotentialSnackbarChange()
  }

  fun notifyPotentialSnackbarChange() {
    asyncDataSubscriptionManager.notifyChangeAsync(SNACKBAR_REQUEST_PROVIDER_ID)
  }

  sealed class SnackbarRequest {
    data class ShowSnackbar(@StringRes val messageStringId: Int, val duration: SnackbarDuration) :
      SnackbarRequest()

    object ShowNothing : SnackbarRequest()
  }

  enum class SnackbarDuration {
    SHORT,
    LONG
  }
}

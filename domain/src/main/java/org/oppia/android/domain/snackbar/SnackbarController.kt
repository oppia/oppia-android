package org.oppia.android.domain.snackbar

import androidx.annotation.StringRes
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_CURRENT_SNACKBAR_REQUEST_PROVIDER_ID =
  "get_current_snackbar_request_provider_id"

/** Controller for enqueueing, dismissing, and retrieving snackbars. */
@Singleton
class SnackbarController @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
) {

  private val _snackbarRequestQueue: Queue<SnackbarRequest.ShowSnackbar> = LinkedList()

  /** Queue for the snackbar request that to be shown in FIFO Behaviour. */
  val snackbarRequestQueue: Queue<SnackbarRequest.ShowSnackbar>
    get() = _snackbarRequestQueue

  /**
   * Gets the snackbar that is enqueued first.
   *
   * @return a [DataProvider] of the current request.
   */
  fun getCurrentSnackbar(): DataProvider<SnackbarRequest> {
    val currentRequest = _snackbarRequestQueue.peek()
    return dataProviders.createInMemoryDataProvider(GET_CURRENT_SNACKBAR_REQUEST_PROVIDER_ID) {
      return@createInMemoryDataProvider currentRequest
        ?: SnackbarRequest.ShowNothing
    }
  }

  /**
   *  Enqueue the snackbar request that to be shown and notify the data provider that it is changed.
   *
   *  @param request the request that is to be added in the queue.
   */
  fun enqueueSnackbar(request: SnackbarRequest.ShowSnackbar) {
    _snackbarRequestQueue.add(request)
    notifyPotentialSnackbarChange()
  }

  /**  Dismiss the current snackbar and notify the data provider that it is changed. */
  fun dismissCurrentSnackbar() {
    _snackbarRequestQueue.remove()
    notifyPotentialSnackbarChange()
  }

  private fun notifyPotentialSnackbarChange() {
    asyncDataSubscriptionManager.notifyChangeAsync(GET_CURRENT_SNACKBAR_REQUEST_PROVIDER_ID)
  }

  /** For the Snackbar Request. */
  sealed class SnackbarRequest {

    /** For showing the snackbar.
     *
     *  @param messageStringId The message string of string resource that is to be displayed.
     *  @param duration The duration for which snackbar is to be shown.
     */
    data class ShowSnackbar(@StringRes val messageStringId: Int, val duration: SnackbarDuration) :
      SnackbarRequest()

    /** For not showing snackbar and dismissing the snackbar if present in the queue. */
    object ShowNothing : SnackbarRequest()
  }

  /**
   * These are for the length of the snackbar that is to be shown.
   */
  enum class SnackbarDuration {
    /** Indicates the short duration of the snackbar */
    SHORT,

    /** Indicates the long duration of the snackbar */
    LONG
  }
}

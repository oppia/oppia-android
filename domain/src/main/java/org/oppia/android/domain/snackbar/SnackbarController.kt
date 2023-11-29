package org.oppia.android.domain.snackbar

import androidx.annotation.StringRes
import com.google.common.util.concurrent.SettableFuture
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Deferred
import org.oppia.android.util.data.AsyncResult

private const val GET_CURRENT_SNACKBAR_REQUEST_PROVIDER_ID =
  "get_current_snackbar_request_provider_id"

/** Controller for enqueueing, dismissing, and retrieving snackbars. */
@Singleton
class SnackbarController @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
) {

  val dismissFuture = SettableFuture.create<Unit>()
  private val _snackbarRequestQueue: Queue<ShowSnackbarRequest> = LinkedList()

  /** Queue of the snackbar requests that are to be shown based on FIFO. */
  val snackbarRequestQueue: Queue<ShowSnackbarRequest>
    get() = _snackbarRequestQueue

  val currentState = CurrentSnackbarState.NotShowing


  fun getCurrentSnackbarState(): DataProvider<CurrentSnackbarState> {

    val currentRequest = _snackbarRequestQueue.peek()

    if (_snackbarRequestQueue.isEmpty()){
      return dataProviders.createInMemoryDataProvider(CurrentSnackbarState.NotShowing){
        return@createInMemoryDataProvider CurrentSnackbarState.NotShowing
      }
    }

//    if ()

  }

  fun enqueueSnackbar(request: ShowSnackbarRequest) {
    _snackbarRequestQueue.add(request)
    notifyPotentialSnackbarChange()
  }

  fun notifySnackbarShowing(snackbarId: Int, onShow: Deferred<Unit>, onDismiss: Deferred<Unit>) {
    // onDismiss is resolved when the snackbar by unique ID snackbarId is no longer showing.
    CurrentSnackbarState.NotShowing

//    val showFuture = onShow.await()

  }

  private fun notifyPotentialSnackbarChange() {
    asyncDataSubscriptionManager.notifyChangeAsync(GET_CURRENT_SNACKBAR_REQUEST_PROVIDER_ID)
  }

  sealed class CurrentSnackbarState {
    object NotShowing : CurrentSnackbarState()

    data class Showing(val request: ShowSnackbarRequest, val snackbarId: Int) :
      CurrentSnackbarState()

    data class WaitingToShow(val nextRequest: ShowSnackbarRequest, val snackbarId: Int) :
      CurrentSnackbarState()
  }

  data class ShowSnackbarRequest(
    @StringRes val messageStringId: Int,
    val duration: SnackbarDuration
  )

  private data class Snackbar(val request: ShowSnackbarRequest, val snackbarId: Int)

  /** These are for the length of the snackbar that is to be shown. */
  enum class SnackbarDuration {

    /** Indicates the short duration of the snackbar. */
    SHORT,

    /** Indicates the long duration of the snackbar. */
    LONG
  }
}

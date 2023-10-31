package org.oppia.android.app.settings.profile

import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.Futures.immediateFuture
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Future
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.snackbar.SnackbarController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import kotlinx.coroutines.Deferred
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders

private const val TAG = "SnackbarManager"
private const val ERROR_MESSAGE = "can't be shown--no activity UI"
private const val GET_CURRENT_SNACKBAR_STATUS_PROVIDER_ID =
  "get_current_snackbar_status_provider_id"

class SnackbarManager @Inject constructor(private val activity: AppCompatActivity, private val snackbarController: SnackbarController) {
  private var currentShowingSnackbarId: Int? = null

  // Must be called by activities wishing to show snackbars.
  fun enableShowingSnackbars(contentView: View) {
    snackbarController.getCurrentSnackbarState().toLiveData().observe(activity) { result ->

      when(result){
        is AsyncResult.Success -> when(val request = result.value) {

          is SnackbarController.CurrentSnackbarState.Showing -> {
//            if (request.snackbarId != currentShowingSnackbarId){
//              snackbarController.snackbarRequestQueue.peek()?.let { showSnackbar(contentView, it) }
//            }

            snackbarController.notifySnackbarShowing(request.snackbarId, )

          }

          is SnackbarController.CurrentSnackbarState.NotShowing -> {

          }

          is SnackbarController.CurrentSnackbarState.WaitingToShow -> {


          }

        }
        else -> {

        }
      }

      // Show a new snackbar if the current state is "showing snackbar" with an ID different than currentShowingSnackbarId.
      // Note that this should automatically handle the case of a new activity being opened before a previous snackbar finished (it should be reshown).
      // Need to call back into SnackbarController via notifySnackbarShowing() to indicate that it's now showing.
    }
  }

  private fun showSnackbar(
    activityView: View,
    showRequest: SnackbarController.ShowSnackbarRequest
  ): Pair<Deferred<Unit>, Deferred<Unit>> {
    val duration = when (showRequest.duration) {
      SnackbarController.SnackbarDuration.SHORT -> Snackbar.LENGTH_SHORT
      SnackbarController.SnackbarDuration.LONG -> Snackbar.LENGTH_LONG
    }

    val showFuture = SettableFuture.create<Unit>()
    val dismissFuture = SettableFuture.create<Unit>()
    Snackbar.make(activityView, showRequest.messageStringId, duration)
      .addCallback(object : Snackbar.Callback() {
        override fun onShown(snackbar: Snackbar) {

//          snackbarController.notifySnackbarShowing()
          showFuture.set(Unit)
        }

        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
          dismissFuture.set(Unit)

        }
      })
      .show()
    // See: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-guava/kotlinx.coroutines.guava/as-deferred.html.
    return showFuture as Deferred<Unit> to dismissFuture as Deferred<Unit>
  }
}
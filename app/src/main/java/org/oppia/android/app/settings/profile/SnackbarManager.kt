package org.oppia.android.app.settings.profile

import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.snackbar.SnackbarController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

private const val TAG = "SnackbarManager"
private const val ERROR_MESSAGE = "can't be shown--no activity UI"

/** Manager for showing snackbars. */
class SnackbarManager @Inject constructor(private val snackbarController: SnackbarController) {

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  /**
   *  Enqueues the snackbar that is be to shown in the FIFO buffer.
   *
   *  @param messageStringId The message string of string resource that is to be displayed.
   *  @param duration The duration for which snackbar is to be shown.
   *  */
  fun showSnackbar(@StringRes messageStringId: Int, duration: SnackbarController.SnackbarDuration) {
    snackbarController.enqueueSnackbar(
      SnackbarController.SnackbarRequest.ShowSnackbar(
        messageStringId,
        duration
      )
    )
  }

  /**
   * Enables the activity to show the snackbar.
   *
   * @param activity For the activity that is to be observed and to upon which the snackbar to be shown.
   */
  fun enableShowingSnackbars(activity: AppCompatActivity) {
    snackbarController.getCurrentSnackbar().toLiveData().observe(activity) { result ->
      when (result) {
        is AsyncResult.Success -> when (val request = result.value) {
          is SnackbarController.SnackbarRequest.ShowSnackbar -> showSnackbar(
            activity.findViewById(
              android.R.id.content
            ),
            request
          )
          SnackbarController.SnackbarRequest.ShowNothing -> {
            if (snackbarController.snackbarRequestQueue.isNotEmpty()) {
              snackbarController.dismissCurrentSnackbar()
            }
          }
        }
        else -> {}
      }
    }
  }

  private fun showSnackbar(
    activityView: View?,
    showRequest: SnackbarController.SnackbarRequest.ShowSnackbar
  ) {

    val duration = when (showRequest.duration) {
      SnackbarController.SnackbarDuration.SHORT -> Snackbar.LENGTH_SHORT
      SnackbarController.SnackbarDuration.LONG -> Snackbar.LENGTH_LONG
    }

    if (activityView == null) {
      oppiaLogger.e(TAG, ERROR_MESSAGE)
    } else {
      Snackbar.make(activityView, showRequest.messageStringId, duration)
        .addCallback(object : Snackbar.Callback() {
          override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            snackbarController.dismissCurrentSnackbar()
          }
        })
        .show()
    }
  }
}

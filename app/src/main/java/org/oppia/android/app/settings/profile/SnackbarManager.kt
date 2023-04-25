package org.oppia.android.app.settings.profile

import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

class SnackbarManager @Inject constructor(private val snackbarController: SnackbarController) {

  fun showSnackbar(@StringRes messageStringId: Int, duration: SnackbarController.SnackbarDuration) {
    snackbarController.enqueueSnackbar(
      SnackbarController.SnackbarRequest.ShowSnackbar(
        messageStringId,
        duration
      )
    )
  }

  fun enableShowingSnackbars(activity: AppCompatActivity) {
    snackbarController.getCurrentSnackbar().toLiveData().observe(activity) { result ->
      when (result) {
        is AsyncResult.Success -> when (val request = result.value) {
          is SnackbarController.SnackbarRequest.ShowSnackbar -> showSnackbar(
            activity.findViewById(
              android.R.id.content
            ), request
          )
          SnackbarController.SnackbarRequest.ShowNothing -> {}
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
      Log.e("SnackbarManager", "can't be shown--no activity UI")
    }
    else {
      Snackbar.make(activityView, showRequest.messageStringId, duration)
        .addCallback(object : Snackbar.Callback() {
          override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            // ...dismiss the snackbar when it's dismissed.
            snackbarController.dismissCurrentSnackbar()
          }
        })
        .show()
    }
  }
}

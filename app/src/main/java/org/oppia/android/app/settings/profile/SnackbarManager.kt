package org.oppia.android.app.settings.profile

import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import kotlinx.android.synthetic.main.profile_edit_fragment.*
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

class SnackbarManager @Inject constructor(private val snackbarController: SnackbarController) {
  // Use SnackbarController for displaying the current snackbar, hiding it after the snackbar hides, or showing a new one.

  fun showSnackbar(@StringRes messageStringId: Int, duration: SnackbarController.SnackbarDuration) {
    // Enqueue snackbar using SnackbarController...
    snackbarController.enqueueSnackbar(
      SnackbarController.SnackbarRequest.ShowSnackbar(
        messageStringId,
        duration
      )
    )
  }

  fun enableShowingSnackbars(activity: AppCompatActivity) {
    // Called in an activity's onCreate to start listening for snackbar state.
    snackbarController.getCurrentSnackbar().toLiveData().observe(activity) { result ->
      when (result) {
        is AsyncResult.Success -> when (val request = result.value) {
          is SnackbarController.SnackbarRequest.ShowSnackbar -> showSnackbar(activity.view, request)
          SnackbarController.SnackbarRequest.ShowNothing -> {} // Do nothing.
        }
        // Other cases...
        else -> {

        }
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
      // ...log an error if activityView is null and ignore the snackbar request (can't be shown--no activity UI).
      Log.e("SnackbarManager", "can't be shown--no activity UI")
    }
    // ...compute length based on showRequest.duration and messageString based on showRequest.messageStringId -- the latter should be done using appResourceHandler (so that the string is in the correct language).
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
      // ...show the snackbar & add a callback for when it dismisses--at that point SnackbarController.dismissCurrentSnackbar() should be called.
    }
  }

  // Abstract domain layer.
//  enum class SnackbarDuration {
//    SHORT,
//    LONG
//  }
}
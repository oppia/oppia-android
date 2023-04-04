package org.oppia.android.app.settings.profile

import androidx.annotation.StringRes
import org.oppia.android.util.data.DataProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
abstract class SnackbarController @Inject constructor() {
  // The class should have a queue of snackbars.

  fun getCurrentSnackbar(): DataProvider<SnackbarRequest> {
    // Return a data provider that will provide the current snackbar request.
    // not getting how to return a data provider
  }

  fun enqueueSnackbar(request: SnackbarRequest.ShowSnackbar) {
    // Enqueue the snackbar request.
    val list = mutableListOf<SnackbarRequest>()
    list.add(request)
  }

  fun dismissCurrentSnackbar() {
    // dismiss the current snackbar
  }

  sealed class SnackbarRequest {
    data class ShowSnackbar(@StringRes val messageStringId: Int, val duration: SnackbarDuration) :
      SnackbarRequest()

    object ShowNothing : SnackbarRequest()
  }

  // Abstraction for Snackbar's length constants.
  enum class SnackbarDuration {
    SHORT,
    LONG
  }
}

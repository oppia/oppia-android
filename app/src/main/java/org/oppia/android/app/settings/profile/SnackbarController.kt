package org.oppia.android.app.settings.profile

import androidx.annotation.StringRes
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

@Singleton
class SnackbarController @Inject constructor(private val dataProviders: DataProviders) {

  private val snackbarRequestQueue: Queue<SnackbarRequest.ShowSnackbar> = LinkedList()

  fun getCurrentSnackbar(): DataProvider<SnackbarRequest> {
    val currentRequest = snackbarRequestQueue.peek()
    return dataProviders.createInMemoryDataProvider(SnackbarRequest::class.java) {
      return@createInMemoryDataProvider currentRequest
        ?: SnackbarRequest.ShowNothing
    }
  }

  fun enqueueSnackbar(request: SnackbarRequest.ShowSnackbar) {
    snackbarRequestQueue.add(request)
  }

  fun dismissCurrentSnackbar() {
    snackbarRequestQueue.remove()
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

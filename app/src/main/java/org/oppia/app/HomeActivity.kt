package org.oppia.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors.directExecutor
import org.oppia.app.model.UserAppHistory
import org.oppia.app.utility.Logger
import org.oppia.util.data.AsyncDataSource

/** The central activity for all users entering the app. */
class HomeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)
  }

  override fun onStart() {
    super.onStart()
    // TODO(BenHenning): Convert this to LiveData rather than risk an async operation completing outside of the
    // activity's lifecycle. Also, a directExecutor() in this context could be a really bad idea if it isn't the main
    // thread.
    Futures.addCallback(getUserAppHistory().pendingOperation, object: FutureCallback<UserAppHistory> {
      override fun onSuccess(result: UserAppHistory?) {
        if (result != null && result.alreadyOpenedApp) {
          getWelcomeTextView().setText(R.string.welcome_back_text)
        }
      }

      override fun onFailure(t: Throwable) {
        // TODO(BenHenning): Replace this log statement with a clearer logging API.
        Logger.e(applicationContext,"HomeActivity", "Failed to retrieve user app history = "+ t)
      }
    }, directExecutor())
  }

  private fun getWelcomeTextView(): TextView {
    return findViewById(R.id.welcome_text_view)
  }

  private fun getUserAppHistory(): AsyncDataSource<UserAppHistory> {
    // TODO(BenHenning): Retrieve this from a domain provider.
    return object: AsyncDataSource<UserAppHistory> {
      override val pendingOperation: ListenableFuture<UserAppHistory>
        get() = Futures.immediateFuture(UserAppHistory.newBuilder().setAlreadyOpenedApp(false).build())
    }
  }
}

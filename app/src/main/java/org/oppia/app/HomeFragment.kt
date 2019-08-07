package org.oppia.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.oppia.app.model.UserAppHistory
import org.oppia.util.data.AsyncDataSource

class HomeFragment: Fragment() {
  private var userHistoryRetrievalJob: Job? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.home_fragment, container, /* attachToRoot= */ false)
  }

  override fun onStart() {
    if (userHistoryRetrievalJob == null) {
      val userAppHistoryDataSource = getUserAppHistory()
      userHistoryRetrievalJob = viewLifecycleOwner.lifecycleScope.launch {
        // TODO(BenHenning): Convert this to LiveData rather than risk an async operation.
        val appHistory = userAppHistoryDataSource.executePendingOperation()
        if (appHistory.alreadyOpenedApp) {
          getWelcomeTextView()?.setText(R.string.welcome_back_text)
        }
      }
    }

    super.onStart()
  }

  private fun getWelcomeTextView(): TextView? {
    return view?.findViewById(R.id.welcome_text_view)
  }

  private fun getUserAppHistory(): AsyncDataSource<UserAppHistory> {
    // TODO(BenHenning): Retrieve this from a domain provider.
    return object: AsyncDataSource<UserAppHistory> {
      override suspend fun executePendingOperation(): UserAppHistory {
        return UserAppHistory.newBuilder().setAlreadyOpenedApp(false).build()
      }
    }
  }
}
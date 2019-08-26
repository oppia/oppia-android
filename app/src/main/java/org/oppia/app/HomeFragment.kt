package org.oppia.app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import org.oppia.app.databinding.HomeFragmentBinding
import org.oppia.app.model.UserAppHistory
import org.oppia.domain.UserAppHistoryController
import org.oppia.util.data.AsyncResult

/** Fragment that contains an introduction to the app. */
class HomeFragment : Fragment() {
  private val userAppHistoryController = UserAppHistoryController()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getUserAppHistoryViewModel()
    val appUserHistory = getUserAppHistory()
    viewModel.userAppHistoryLiveData = appUserHistory
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = this
    }

    // TODO(#70): Mark that the user opened the app once it's persisted to disk.

    return binding.root
  }

  private fun getUserAppHistoryViewModel(): UserAppHistoryViewModel {
    return ViewModelProviders.of(this).get(UserAppHistoryViewModel::class.java)
  }

  private fun getUserAppHistory(): LiveData<UserAppHistory> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(
      userAppHistoryController.getUserAppHistory()
    ) { result: AsyncResult<UserAppHistory> -> processUserAppHistoryResult(result) }
  }

  private fun processUserAppHistoryResult(appHistoryResult: AsyncResult<UserAppHistory>): UserAppHistory {
    if (appHistoryResult.isFailure()) {
      Log.e("HomeFragment", "Failed to retrieve user app history", appHistoryResult.error)
    }
    return appHistoryResult.getOrDefault(UserAppHistory.getDefaultInstance())
  }
}

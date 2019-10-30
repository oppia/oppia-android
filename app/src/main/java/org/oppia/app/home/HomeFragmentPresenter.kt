package org.oppia.app.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.databinding.HomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.story.StoryActivity
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.UserAppHistoryController
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import org.oppia.domain.topic.TEST_STORY_ID_1
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val EXPLORATION_ID = TEST_EXPLORATION_ID_5

/** The controller for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<UserAppHistoryViewModel>,
  private val userAppHistoryController: UserAppHistoryController,
  private val explorationDataController: ExplorationDataController,
  private val logger: Logger
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.viewModel = getUserAppHistoryViewModel()
      it.presenter = this
      it.lifecycleOwner = fragment
    }

    userAppHistoryController.markUserOpenedApp()

    return binding.root
  }

  private fun getUserAppHistoryViewModel(): UserAppHistoryViewModel {
    return viewModelProvider.getForFragment(fragment, UserAppHistoryViewModel::class.java)
  }

  fun playExplorationButton(v: View) {
    explorationDataController.startPlayingExploration(
      EXPLORATION_ID
    ).observe(fragment, Observer<AsyncResult<Any?>> { result ->
      when {
        result.isPending() -> logger.d("HomeFragment", "Loading exploration")
        result.isFailure() -> logger.e("HomeFragment", "Failed to load exploration", result.getErrorOrNull()!!)
        else -> {
          logger.d("HomeFragment", "Successfully loaded exploration")
          routeToExplorationListener.routeToExploration(EXPLORATION_ID)
        }
      }
    })
  }

  // TODO(#134): Remove this method once it is possible to navigate to story activity in normal flow
  fun openStory(v: View) {
    val intent = StoryActivity.createStoryActivityIntent(fragment.activity as Context, TEST_STORY_ID_1)
    fragment.activity?.startActivity(intent)
  }
}

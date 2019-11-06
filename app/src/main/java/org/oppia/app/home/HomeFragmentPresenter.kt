package org.oppia.app.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.databinding.HomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.topiclist.PromotedStoryViewModel
import org.oppia.app.home.topiclist.TopicListAdapter
import org.oppia.app.home.topiclist.TopicSummaryClickListener
import org.oppia.app.home.topiclist.TopicSummaryViewModel
import org.oppia.app.model.TopicList
import org.oppia.app.model.TopicSummary
import org.oppia.app.model.UserAppHistory
import org.oppia.domain.UserAppHistoryController
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val EXPLORATION_ID = TEST_EXPLORATION_ID_5
private const val TAG_HOME_FRAGMENT = "HomeFragment"

/** The presenter for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val userAppHistoryController: UserAppHistoryController,
  private val topicListController: TopicListController,
  private val explorationDataController: ExplorationDataController,
  private val logger: Logger
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener
  private val routeToTopicListener = activity as RouteToTopicListener

  private val itemList: MutableList<HomeItemViewModel> = ArrayList()

  private lateinit var topicListAdapter: TopicListAdapter

  private lateinit var binding: HomeFragmentBinding
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.

    topicListAdapter = TopicListAdapter(itemList)

    val homeLayoutManager = GridLayoutManager(activity.applicationContext, 2)
    homeLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position == 0 || position == 1) {
          /* number of spaces this item should occupy = */ 2
        } else {
          /* number of spaces this item should occupy = */ 1
        }
      }
    }

    binding.homeRecyclerView.apply {
      adapter = topicListAdapter
      // https://stackoverflow.com/a/32763434/32763621
      layoutManager = homeLayoutManager
    }
    binding.let {
      it.presenter = this
      it.lifecycleOwner = fragment
    }

    userAppHistoryController.markUserOpenedApp()

    subscribeToUserAppHistory()
    subscribeToTopicList()

    return binding.root
  }

  fun playExplorationButton(v: View) {
    explorationDataController.startPlayingExploration(
      EXPLORATION_ID
    ).observe(fragment, Observer<AsyncResult<Any?>> { result ->
      when {
        result.isPending() -> logger.d(TAG_HOME_FRAGMENT, "Loading exploration")
        result.isFailure() -> logger.e(TAG_HOME_FRAGMENT, "Failed to load exploration", result.getErrorOrNull()!!)
        else -> {
          logger.d(TAG_HOME_FRAGMENT, "Successfully loaded exploration")
          routeToExplorationListener.routeToExploration(EXPLORATION_ID)
        }
      }
    })
  }

  fun handleTopicButtonClicked(v: View) {
    logger.d(TAG_HOME_FRAGMENT, "Successfully loaded topic")
    routeToTopicListener.routeToTopic(TEST_TOPIC_ID_0)
  }

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }

  private fun subscribeToTopicList() {
    getAssumedSuccessfulTopicList().observe(fragment, Observer<TopicList> { result ->

      val promotedStoryViewModel = PromotedStoryViewModel(activity)
      promotedStoryViewModel.setPromotedStory(result.promotedStory)
      itemList.add(promotedStoryViewModel)
      for (topicSummary in result.topicSummaryList) {
        val topicSummaryViewModel = TopicSummaryViewModel(topicSummary, fragment as TopicSummaryClickListener)
        itemList.add(topicSummaryViewModel)
      }
      topicListAdapter.notifyDataSetChanged()
    })
  }

  private fun getAssumedSuccessfulTopicList(): LiveData<TopicList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(topicListSummaryResultLiveData) { it.getOrDefault(TopicList.getDefaultInstance()) }
  }

  private fun subscribeToUserAppHistory() {
    getUserAppHistory().observe(fragment, Observer<UserAppHistory> { result ->
      val userAppHistoryViewModel = UserAppHistoryViewModel()
      userAppHistoryViewModel.setAlreadyAppOpened(result.alreadyOpenedApp)
      itemList.add(0, userAppHistoryViewModel)
      topicListAdapter.notifyDataSetChanged()
    })
  }

  private fun getUserAppHistory(): LiveData<UserAppHistory> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(userAppHistoryController.getUserAppHistory(), ::processUserAppHistoryResult)
  }

  private fun processUserAppHistoryResult(appHistoryResult: AsyncResult<UserAppHistory>): UserAppHistory {
    if (appHistoryResult.isFailure()) {
      logger.e("HomeFragment", "Failed to retrieve user app history" + appHistoryResult.getErrorOrNull())
    }
    return appHistoryResult.getOrDefault(UserAppHistory.getDefaultInstance())
  }

  fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    routeToTopicListener.routeToTopic(topicSummary.topicId)
  }
}

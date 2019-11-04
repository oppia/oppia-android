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
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.UserAppHistoryController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The controller for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<UserAppHistoryViewModel>,
  private val userAppHistoryController: UserAppHistoryController,
  private val topicListController: TopicListController,
  private val logger: Logger
) {
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

  private fun getUserAppHistoryViewModel(): UserAppHistoryViewModel {
    return viewModelProvider.getForFragment(fragment, UserAppHistoryViewModel::class.java)
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
      getUserAppHistoryViewModel().setAlreadyAppOpened(result.alreadyOpenedApp)
      itemList.add(0, getUserAppHistoryViewModel())
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

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
import org.oppia.app.model.TopicList
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.UserAppHistoryController
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val EXPLORATION_ID = TEST_EXPLORATION_ID_5

/** The controller for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<UserAppHistoryViewModel>,
  private val promotedStoryViewModel: ViewModelProvider<PromotedStoryViewModel>,
  private val userAppHistoryController: UserAppHistoryController,
  private val explorationDataController: ExplorationDataController,
  private val topicListController: TopicListController,
  private val logger: Logger
) {

  private val itemList: MutableList<Any> = ArrayList()

  private lateinit var topicListAdapter: TopicListAdapter

  private lateinit var binding: HomeFragmentBinding

  private val routeToExplorationListener = activity as RouteToExplorationListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.

    topicListAdapter = TopicListAdapter(itemList)

    val homeLayoutManager = GridLayoutManager(activity.applicationContext, 2)
    homeLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position == 0) {
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
      it.viewModel = getUserAppHistoryViewModel()
      it.presenter = this
      it.lifecycleOwner = fragment
    }

    userAppHistoryController.markUserOpenedApp()

    subscribeToTopicList()

    return binding.root
  }

  private fun getUserAppHistoryViewModel(): UserAppHistoryViewModel {
    return viewModelProvider.getForFragment(fragment, UserAppHistoryViewModel::class.java)
  }

  private fun getPromotedStoryViewModel(): PromotedStoryViewModel? {
    return promotedStoryViewModel.getForFragment(fragment, PromotedStoryViewModel::class.java)
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

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }

  private fun subscribeToTopicList() {
    getAssumedSuccessfulTopicList().observe(fragment, Observer<TopicList> { result ->
      getPromotedStoryViewModel()!!.setPromotedStory(result.promotedStory)
      if (getPromotedStoryViewModel() != null) {
        itemList.add(getPromotedStoryViewModel()!!)
      }
      itemList.addAll(result.topicSummaryList)
      topicListAdapter.notifyDataSetChanged()
    })
  }

  private fun getAssumedSuccessfulTopicList(): LiveData<TopicList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(topicListSummaryResultLiveData) { it.getOrDefault(TopicList.getDefaultInstance()) }
  }

  private fun getWhetherTopicListLookupSucceeded(): LiveData<Boolean> {
    return Transformations.map(topicListSummaryResultLiveData) { it.isSuccess() }
  }

  private fun getWhetherTopicListLookupFailed(): LiveData<Boolean> {
    return Transformations.map(topicListSummaryResultLiveData) { it.isFailure() }
  }

  private fun getWhetherTopicListIsLoading(): LiveData<Boolean> {
    return Transformations.map(topicListSummaryResultLiveData) { it.isPending() }
  }

  private fun <T> expandList(list: List<T>): List<T> {
    val vals = mutableListOf<T>()
    vals += list
    vals += list
    vals += list
    vals += list
    vals += list
    vals += list
    vals += list
    vals += list
    vals += list
    vals += list
    vals += list
    vals += list
    vals += list
    return vals
  }

}

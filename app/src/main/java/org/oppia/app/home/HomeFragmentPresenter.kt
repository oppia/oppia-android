package org.oppia.app.home

import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.viewmodel.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.databinding.AllTopicsBinding
import org.oppia.app.databinding.HomeFragmentBinding
import org.oppia.app.databinding.PromotedStoryCardBinding
import org.oppia.app.databinding.PromotedStoryCardBindingImpl
import org.oppia.app.databinding.PromotedStoryListBinding
import org.oppia.app.databinding.TopicSummaryViewBinding
import org.oppia.app.databinding.WelcomeBinding
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.topiclist.AllTopicsViewModel
import org.oppia.app.home.topiclist.PromotedStoryListViewModel
import org.oppia.app.home.topiclist.PromotedStoryViewModel
import org.oppia.app.home.topiclist.TopicListAdapter
import org.oppia.app.home.topiclist.TopicSummaryClickListener
import org.oppia.app.home.topiclist.TopicSummaryViewModel
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.TopicList
import org.oppia.app.model.TopicSummary
import org.oppia.app.profileprogress.RecentlyPlayedStorySummaryViewModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import org.oppia.util.datetime.DateTimeUtil
import org.oppia.util.logging.Logger
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** The presenter for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HomeListViewModel>
) {
  private val routeToTopicListener = activity as RouteToTopicListener
  private lateinit var binding: HomeFragmentBinding
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId

  private val orientation = Resources.getSystem().configuration.orientation

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.


    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    val viewModel = getHomeListViewModel()
    viewModel.setProfileId(internalProfileId)

    val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      2
    } else {
      3
    }

    val homeLayoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    homeLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position == 0 || position == 1 || position == 2) {
          /* number of spaces this item should occupy = */ spanCount
        } else {
          /* number of spaces this item should occupy = */ 1
        }
      }
    }

    binding.homeRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
      // https://stackoverflow.com/a/32763434/32763621
      layoutManager = homeLayoutManager
    }
    binding.let {
      it.presenter = this
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<HomeItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<HomeItemViewModel, HomeItemViewModel.ViewType> { viewModel ->
        when (viewModel) {
          is WelcomeViewModel -> HomeItemViewModel.ViewType.VIEW_TYPE_WELCOME_MESSAGE
          is PromotedStoryViewModel -> HomeItemViewModel.ViewType.VIEW_TYPE_PROMOTED_STORY_LIST
          is AllTopicsViewModel -> HomeItemViewModel.ViewType.VIEW_TYPE_ALL_TOPICS
          is TopicSummaryViewModel -> HomeItemViewModel.ViewType.VIEW_TYPE_TOPIC_LIST
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = HomeItemViewModel.ViewType.VIEW_TYPE_WELCOME_MESSAGE,
        inflateDataBinding = WelcomeBinding::inflate,
        setViewModel = WelcomeBinding::setViewModel,
        transformViewModel = { it as WelcomeViewModel }
      )
      .registerViewDataBinder(
        viewType = HomeItemViewModel.ViewType.VIEW_TYPE_PROMOTED_STORY_LIST,
        inflateDataBinding = PromotedStoryCardBinding::inflate,
        setViewModel = PromotedStoryCardBinding::setViewModel,
        transformViewModel = { it as PromotedStoryViewModel }
      )
      .registerViewDataBinder(
        viewType = HomeItemViewModel.ViewType.VIEW_TYPE_ALL_TOPICS,
        inflateDataBinding = AllTopicsBinding::inflate,
        setViewModel = AllTopicsBinding::setViewModel,
        transformViewModel = { it as AllTopicsViewModel }
      )
      .registerViewDataBinder(
        viewType = HomeItemViewModel.ViewType.VIEW_TYPE_TOPIC_LIST,
        inflateDataBinding = TopicSummaryViewBinding::inflate,
        setViewModel = TopicSummaryViewBinding::setViewModel,
        transformViewModel = { it as TopicSummaryViewModel }
      )
      .build()
  }

  private fun getHomeListViewModel(): HomeListViewModel {
    return viewModelProvider.getForFragment(fragment, HomeListViewModel::class.java)
  }

  fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    routeToTopicListener.routeToTopic(internalProfileId, topicSummary.topicId)
  }
}

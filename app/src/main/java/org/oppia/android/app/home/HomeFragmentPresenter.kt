package org.oppia.android.app.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.PromotedStoryListAdapter
import org.oppia.android.app.home.topiclist.PromotedStoryListViewModel
import org.oppia.android.app.home.topiclist.PromotedStoryViewModel
import org.oppia.android.app.home.topiclist.TopicListAdapter
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OngoingStoryList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.StartSnapHelper
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.AllTopicsBinding
import org.oppia.android.databinding.PromotedStoryListBinding
import org.oppia.android.databinding.TopicSummaryViewBinding
import org.oppia.android.databinding.WelcomeBinding
import org.oppia.android.databinding.HomeFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.StoryHtmlParserEntityType
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** The presenter for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val welcomeViewModelProvider: ViewModelProvider<WelcomeViewModel>,
  private val promotedStoryListViewModelProvider: ViewModelProvider<PromotedStoryListViewModel>,
  private val allTopicsViewModelProvider: ViewModelProvider<AllTopicsViewModel>,
  private val topicListController: TopicListController,
  private val oppiaClock: OppiaClock,
  private val oppiaLogger: OppiaLogger,
  private val intentFactoryShim: IntentFactoryShim,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  @StoryHtmlParserEntityType private val storyEntityType: String
) {
  private val routeToTopicListener = activity as RouteToTopicListener
  private val itemList: MutableList<HomeItemViewModel> = ArrayList()
  private lateinit var binding: HomeFragmentBinding
  private var internalProfileId: Int = -1
//  private lateinit var topicListAdapter: TopicListAdapter
  private var spanCount = 0

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.

    val welcomeViewModel = getWelcomeViewModel()
    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    welcomeViewModel.setInternalProfileId(internalProfileId)
    logHomeActivityEvent()

    val promotedStoryListViewModel = getPromotedStoryListViewModel()
    promotedStoryListViewModel.setInternalProfileId(internalProfileId)
    promotedStoryListViewModel.setActivity(activity)
    promotedStoryListViewModel.setIntentFactoryShim(intentFactoryShim)
    val allTopicsViewModel = getAllTopicsViewModel()
    itemList.add(welcomeViewModel)
    itemList.add(promotedStoryListViewModel)
    itemList.add(allTopicsViewModel)
    spanCount = activity.resources.getInteger(R.integer.home_span_count)

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
      it.lifecycleOwner = fragment
    }

    subscribeToTopicList()
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<HomeItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<HomeItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is WelcomeViewModel -> ViewType.VIEW_TYPE_WELCOME_MESSAGE
          is PromotedStoryListViewModel -> ViewType.VIEW_TYPE_PROMOTED_STORY_LIST
          is AllTopicsViewModel -> ViewType.VIEW_TYPE_ALL_TOPICS
          is TopicSummaryViewModel -> ViewType.VIEW_TYPE_TOPIC_LIST
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_WELCOME_MESSAGE,
        inflateDataBinding = WelcomeBinding::inflate,
        setViewModel = WelcomeBinding::setViewModel,
        transformViewModel = { it as WelcomeViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_PROMOTED_STORY_LIST,
        inflateDataBinding = PromotedStoryListBinding::inflate,
        setViewModel = this::bindPromotedStoryListView,
        transformViewModel = { it as PromotedStoryListViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_ALL_TOPICS,
        inflateDataBinding = AllTopicsBinding::inflate,
        setViewModel = AllTopicsBinding::setViewModel,
        transformViewModel = { it as AllTopicsViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_TOPIC_LIST,
        inflateDataBinding = TopicSummaryViewBinding::inflate,
        setViewModel = TopicSummaryViewBinding::setViewModel,
        transformViewModel = { it as TopicSummaryViewModel }
      )
      .build()
  }

  inner class TopicListViewHolder(val binding: TopicSummaryViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    internal fun bind(topicSummaryViewModel: TopicSummaryViewModel, position: Int) {
      binding.viewModel = topicSummaryViewModel

      val marginLayoutParams = binding.topicContainer.layoutParams as ViewGroup.MarginLayoutParams

      val marginMax = (activity as Context).resources.getDimensionPixelSize(R.dimen.home_margin_max)

      val marginTopBottom =
        (activity as Context).resources
          .getDimensionPixelSize(R.dimen.topic_list_item_margin_top_bottom)

      val marginMin = (activity as Context).resources.getDimensionPixelSize(R.dimen.home_margin_min)

      when (spanCount) {
        2 -> {
          when {
            position % spanCount == 0 -> marginLayoutParams.setMargins(
              marginMin,
              marginTopBottom,
              marginMax,
              marginTopBottom
            )
            else -> marginLayoutParams.setMargins(
              marginMax,
              marginTopBottom,
              marginMin,
              marginTopBottom
            )
          }
        }
        3 -> {
          when {
            position % spanCount == 0 -> marginLayoutParams.setMargins(
              marginMax,
              marginTopBottom,
              /* right= */ 0,
              marginTopBottom
            )
            position % spanCount == 1 -> marginLayoutParams.setMargins(
              marginMin,
              marginTopBottom,
              marginMin,
              marginTopBottom
            )
            position % spanCount == 2 -> marginLayoutParams.setMargins(
              /* left= */ 0,
              marginTopBottom,
              marginMax,
              marginTopBottom
            )
          }
        }
        4 -> {
          when {
            (position + 1) % spanCount == 0 -> marginLayoutParams.setMargins(
              marginMax,
              marginTopBottom,
              /* right= */ 0,
              marginTopBottom
            )
            (position + 1) % spanCount == 1 -> marginLayoutParams.setMargins(
              marginMin,
              marginTopBottom,
              marginMin / 2,
              marginTopBottom
            )
            (position + 1) % spanCount == 2 -> marginLayoutParams.setMargins(
              marginMin / 2,
              marginTopBottom,
              marginMin,
              marginTopBottom
            )
            (position + 1) % spanCount == 3 -> marginLayoutParams.setMargins(
              /* left= */ 0,
              marginTopBottom,
              marginMax,
              marginTopBottom
            )
          }
        }
      }
      binding.topicContainer.layoutParams = marginLayoutParams
    }

  }

  private fun bindPromotedStoryListView(
    binding: PromotedStoryListBinding,
    model: PromotedStoryListViewModel
  ) {
    binding.viewModel = model
    if (activity.resources.getBoolean(R.bool.isTablet)) {
      binding.itemCount = model.promotedStoryListLiveData.value!!.size
    }
    val promotedStoryAdapter = PromotedStoryListAdapter(activity, model.promotedStoryListLiveData.value!!)
    val horizontalLayoutManager =
      LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, /* reverseLayout= */ false)
    binding.promotedStoryListRecyclerView.apply {
      layoutManager = horizontalLayoutManager
      adapter = promotedStoryAdapter
    }

    /*
     * The StartSnapHelper is used to snap between items rather than smooth scrolling,
     * so that the item is completely visible in [HomeFragment] as soon as learner lifts the finger after scrolling.
     */
    val snapHelper = StartSnapHelper()
    binding.promotedStoryListRecyclerView.layoutManager = horizontalLayoutManager
    binding.promotedStoryListRecyclerView.setOnFlingListener(null)
    snapHelper.attachToRecyclerView(binding.promotedStoryListRecyclerView)

    val paddingEnd =
      (activity as Context).resources.getDimensionPixelSize(R.dimen.home_padding_end)
    val paddingStart =
      (activity as Context).resources.getDimensionPixelSize(R.dimen.home_padding_start)
    if (model.promotedStoryListLiveData.value!!.size > 1) {
      binding.promotedStoryListRecyclerView.setPadding(paddingStart, 0, paddingEnd, 0)
    } else {
      binding.promotedStoryListRecyclerView.setPadding(paddingStart, 0, paddingStart, 0)
    }
  }

  private enum class ViewType {
    VIEW_TYPE_WELCOME_MESSAGE,
    VIEW_TYPE_PROMOTED_STORY_LIST,
    VIEW_TYPE_ALL_TOPICS,
    VIEW_TYPE_TOPIC_LIST
  }

  private fun getWelcomeViewModel(): WelcomeViewModel {
    return welcomeViewModelProvider.getForFragment(fragment, WelcomeViewModel::class.java)
  }

  private fun getPromotedStoryListViewModel(): PromotedStoryListViewModel {
    return promotedStoryListViewModelProvider
      .getForFragment(fragment, PromotedStoryListViewModel::class.java)
  }

  private fun getAllTopicsViewModel(): AllTopicsViewModel {
    return allTopicsViewModelProvider.getForFragment(fragment, AllTopicsViewModel::class.java)
  }

  private val topicListSummaryResultLiveData: LiveData<AsyncResult<TopicList>> by lazy {
    topicListController.getTopicList()
  }

  private fun subscribeToTopicList() {
    getAssumedSuccessfulTopicList().observe(
      fragment,
      Observer<TopicList> { result ->
        for (topicSummary in result.topicSummaryList) {
          val topicSummaryViewModel =
            TopicSummaryViewModel(
              topicSummary,
              topicEntityType,
              fragment as TopicSummaryClickListener
            )
          itemList.add(topicSummaryViewModel)
        }
//        topicListAdapter.notifyDataSetChanged()
      }
    )
  }

  private fun getAssumedSuccessfulTopicList(): LiveData<TopicList> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(topicListSummaryResultLiveData) {
      it.getOrDefault(TopicList.getDefaultInstance())
    }
  }

  fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    routeToTopicListener.routeToTopic(internalProfileId, topicSummary.topicId)
  }

  private fun logHomeActivityEvent() {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_HOME,
      /* eventContext= */ null
    )
  }
}

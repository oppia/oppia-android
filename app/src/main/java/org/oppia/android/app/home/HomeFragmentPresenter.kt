package org.oppia.android.app.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.promotedlist.ComingSoonTopicListViewModel
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.datetime.DateTimeUtil
import org.oppia.android.databinding.AllTopicsBinding
import org.oppia.android.databinding.ComingSoonTopicListBinding
import org.oppia.android.databinding.HomeFragmentBinding
import org.oppia.android.databinding.PromotedStoryListBinding
import org.oppia.android.databinding.TopicSummaryViewBinding
import org.oppia.android.databinding.WelcomeBinding
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The presenter for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  @StoryHtmlParserEntityType private val storyEntityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val dateTimeUtil: DateTimeUtil,
  private val translationController: TranslationController,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  @EnableOnboardingFlowV2
  private val enableOnboardingFlowV2: PlatformParameterValue<Boolean>,
  private val appStartupStateController: AppStartupStateController
) {
  private val routeToTopicPlayStoryListener = activity as RouteToTopicPlayStoryListener
  private val exitProfileListener = activity as ExitProfileListener

  private lateinit var binding: HomeFragmentBinding
  private var internalProfileId: Int = -1
  private var profileId: ProfileId = ProfileId.getDefaultInstance()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.

    profileId = activity.intent.extractCurrentUserProfileId()
    internalProfileId = profileId.internalId

    logHomeActivityEvent()

    val homeViewModel = HomeViewModel(
      activity,
      fragment,
      oppiaLogger,
      internalProfileId,
      profileManagementController,
      topicListController,
      topicEntityType,
      storyEntityType,
      resourceHandler,
      dateTimeUtil,
      translationController
    )

    val homeAdapter = createRecyclerViewAdapter()
    val spanCount = activity.resources.getInteger(R.integer.home_span_count)
    val homeLayoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    homeLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position < homeAdapter.itemCount &&
          homeAdapter.getItemViewType(position) == ViewType.TOPIC_LIST.ordinal
        ) 1
        else spanCount
      }
    }
    binding.homeRecyclerView.apply {
      adapter = homeAdapter
      layoutManager = homeLayoutManager
    }

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = homeViewModel
    }

    profileManagementController.getProfile(profileId).toLiveData().observe(fragment) {
      processProfileResult(it)
    }

    return binding.root
  }

  private fun processProfileResult(result: AsyncResult<Profile>) {
    when (result) {
      is AsyncResult.Success -> {
        val profile = result.value
        val profileType = profile.profileType

        if (enableOnboardingFlowV2.value && !profile.completedProfileOnboarding) {
          // These asynchronous API calls do not block or wait for their results. They execute in
          // the background and have minimal chances of interfering with the synchronous
          // `handleBackPress` call below.
          profileManagementController.markProfileOnboardingEnded(profileId)
          if (profileType == ProfileType.SOLE_LEARNER || profileType == ProfileType.SUPERVISOR) {
            appStartupStateController.markOnboardingFlowCompleted(profileId)
          }
        }

        // This synchronous function call executes independently of the async calls above.
        handleBackPress(profileType)
      }
      is AsyncResult.Failure -> {
        oppiaLogger.e("HomeFragment", "Failed to fetch profile with id:$profileId", result.error)
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> {
        Profile.getDefaultInstance()
      }
    }
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<HomeItemViewModel> {
    return multiTypeBuilderFactory.create<HomeItemViewModel, ViewType> { viewModel ->
      when (viewModel) {
        is WelcomeViewModel -> ViewType.WELCOME_MESSAGE
        is PromotedStoryListViewModel -> ViewType.PROMOTED_STORY_LIST
        is ComingSoonTopicListViewModel -> ViewType.COMING_SOON_TOPIC_LIST
        is AllTopicsViewModel -> ViewType.ALL_TOPICS
        is TopicSummaryViewModel -> ViewType.TOPIC_LIST
        else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
      }
    }
      .registerViewDataBinder(
        viewType = ViewType.WELCOME_MESSAGE,
        inflateDataBinding = WelcomeBinding::inflate,
        setViewModel = WelcomeBinding::setViewModel,
        transformViewModel = { it as WelcomeViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.PROMOTED_STORY_LIST,
        inflateDataBinding = PromotedStoryListBinding::inflate,
        setViewModel = PromotedStoryListBinding::setViewModel,
        transformViewModel = { it as PromotedStoryListViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.COMING_SOON_TOPIC_LIST,
        inflateDataBinding = ComingSoonTopicListBinding::inflate,
        setViewModel = ComingSoonTopicListBinding::setViewModel,
        transformViewModel = { it as ComingSoonTopicListViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.ALL_TOPICS,
        inflateDataBinding = AllTopicsBinding::inflate,
        setViewModel = AllTopicsBinding::setViewModel,
        transformViewModel = { it as AllTopicsViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.TOPIC_LIST,
        inflateDataBinding = TopicSummaryViewBinding::inflate,
        setViewModel = TopicSummaryViewBinding::setViewModel,
        transformViewModel = { it as TopicSummaryViewModel }
      )
      .build()
  }

  private enum class ViewType {
    WELCOME_MESSAGE,
    PROMOTED_STORY_LIST,
    COMING_SOON_TOPIC_LIST,
    ALL_TOPICS,
    TOPIC_LIST
  }

  fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    routeToTopicPlayStoryListener.routeToTopicPlayStory(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicSummary.classroomId,
      topicSummary.topicId,
      topicSummary.firstStoryId
    )
  }

  private fun logHomeActivityEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenHomeContext(),
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build()
    )
  }

  private fun handleBackPress(profileType: ProfileType) {
    activity.onBackPressedDispatcher.addCallback(
      fragment,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          exitProfileListener.exitProfile(profileType)
          // The dispatcher can hold a reference to the host
          // so we need to null it out to prevent memory leaks.
          this.remove()
        }
      }
    )
  }
}

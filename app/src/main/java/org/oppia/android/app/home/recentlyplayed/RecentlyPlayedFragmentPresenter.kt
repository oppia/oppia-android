package org.oppia.android.app.home.recentlyplayed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.databinding.RecentlyPlayedFragmentBinding
import org.oppia.android.databinding.RecentlyPlayedStoryCardBinding
import org.oppia.android.databinding.SectionTitleBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [RecentlyPlayedFragment]. */
@FragmentScope
class RecentlyPlayedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val explorationDataController: ExplorationDataController,
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  private val recentlyPlayedViewModelFactory: RecentlyPlayedViewModel.Factory
) {

  private val routeToResumeLessonListener = activity as RouteToResumeLessonListener
  private val routeToExplorationListener = activity as RouteToExplorationListener

  private lateinit var profileId: ProfileId
  private lateinit var binding: RecentlyPlayedFragmentBinding

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
  ): View? {
    this.profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    val recentlyPlayedViewModel = recentlyPlayedViewModelFactory.create(
      fragment as PromotedStoryClickListener,
      this.profileId
    )
    binding =
      RecentlyPlayedFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false).apply {
        lifecycleOwner = fragment
        viewModel = recentlyPlayedViewModel
        val adapter = createRecyclerViewAdapter()
        ongoingStoryRecyclerView.layoutManager = createLayoutManager(adapter)
        ongoingStoryRecyclerView.adapter = adapter
      }

    return binding.root
  }

  private fun createLayoutManager(
    adapter: BindableAdapter<RecentlyPlayedItemViewModel>
  ): RecyclerView.LayoutManager {
    val spanCount = activity.resources.getInteger(R.integer.recently_played_span_count)
    val layoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (adapter.getItemViewType(position) == ViewType.VIEW_TYPE_TITLE.ordinal) {
          spanCount
        } else {
          1
        }
      }
    }
    return layoutManager
  }

  fun promotedStoryClicked(promotedStory: PromotedStory) {
    val canHavePartialProgressSaved =
      when (promotedStory.chapterPlayState) {
        ChapterPlayState.IN_PROGRESS_SAVED, ChapterPlayState.IN_PROGRESS_NOT_SAVED,
        ChapterPlayState.STARTED_NOT_COMPLETED, ChapterPlayState.NOT_STARTED -> true
        ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED,
        ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES, ChapterPlayState.UNRECOGNIZED,
        ChapterPlayState.COMPLETED, null -> false
      }
    if (promotedStory.chapterPlayState == ChapterPlayState.IN_PROGRESS_SAVED) {
      val explorationCheckpointLiveData =
        explorationCheckpointController.retrieveExplorationCheckpoint(
          profileId, promotedStory.explorationId
        ).toLiveData()

      explorationCheckpointLiveData.observe(
        fragment,
        object : Observer<AsyncResult<ExplorationCheckpoint>> {
          override fun onChanged(it: AsyncResult<ExplorationCheckpoint>) {
            if (it is AsyncResult.Success) {
              explorationCheckpointLiveData.removeObserver(this)
              routeToResumeLessonListener.routeToResumeLesson(
                profileId,
                promotedStory.classroomId,
                promotedStory.topicId,
                promotedStory.storyId,
                promotedStory.explorationId,
                parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED,
                explorationCheckpoint = it.value
              )
            } else if (it is AsyncResult.Failure) {
              explorationCheckpointLiveData.removeObserver(this)
              playExploration(
                promotedStory.classroomId,
                promotedStory.topicId,
                promotedStory.storyId,
                promotedStory.explorationId,
                canHavePartialProgressSaved
              )
            }
          }
        }
      )
    } else {
      playExploration(
        promotedStory.classroomId,
        promotedStory.topicId,
        promotedStory.storyId,
        promotedStory.explorationId,
        canHavePartialProgressSaved
      )
    }
  }

  private enum class ViewType {
    VIEW_TYPE_TITLE,
    VIEW_TYPE_PROMOTED_STORY
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<RecentlyPlayedItemViewModel> {
    return multiTypeBuilderFactory.create<RecentlyPlayedItemViewModel, ViewType> { viewModel ->
      when (viewModel) {
        is PromotedStoryViewModel -> ViewType.VIEW_TYPE_PROMOTED_STORY
        is SectionTitleViewModel -> ViewType.VIEW_TYPE_TITLE
        else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
      }
    }.registerViewDataBinder(
      viewType = ViewType.VIEW_TYPE_TITLE,
      inflateDataBinding = SectionTitleBinding::inflate,
      setViewModel = SectionTitleBinding::setViewModel,
      transformViewModel = { it as SectionTitleViewModel }
    ).registerViewDataBinder(
      viewType = ViewType.VIEW_TYPE_PROMOTED_STORY,
      inflateDataBinding = RecentlyPlayedStoryCardBinding::inflate,
      setViewModel = RecentlyPlayedStoryCardBinding::setViewModel,
      transformViewModel = { it as PromotedStoryViewModel }
    ).build()
  }

  private fun playExploration(
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    canHavePartialProgressSaved: Boolean
  ) {
    val startPlayingProvider = if (canHavePartialProgressSaved) {
      // Regardless of whether there's saved progress, this is always a restart. Either the
      // exploration had progress but it was failed to be retrieved, or its partial progress was
      // never originally saved (either due to being pre-checkpoint, or failing to save). In all
      // cases, lessons played from this fragment are known to be in progress, and that progress
      // can't be resumed here (hence the restart).
      explorationDataController.restartExploration(
        profileId.internalId, classroomId, topicId, storyId, explorationId
      )
    } else {
      // The only lessons that can't have their progress saved are those that were already
      // completed.
      explorationDataController.replayExploration(
        profileId.internalId, classroomId, topicId, storyId, explorationId
      )
    }
    startPlayingProvider.toLiveData().observe(fragment) { result ->
      when (result) {
        is AsyncResult.Pending -> oppiaLogger.d("RecentlyPlayedFragment", "Loading exploration")
        is AsyncResult.Failure ->
          oppiaLogger.e("RecentlyPlayedFragment", "Failed to load exploration", result.error)
        is AsyncResult.Success -> {
          oppiaLogger.d("RecentlyPlayedFragment", "Successfully loaded exploration")
          routeToExplorationListener.routeToExploration(
            profileId,
            classroomId,
            topicId,
            storyId,
            explorationId,
            parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED,
            isCheckpointingEnabled = canHavePartialProgressSaved
          )
          activity.finish()
        }
      }
    }
  }
}

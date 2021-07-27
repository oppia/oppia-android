package org.oppia.android.app.resumelesson

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ResumeLessonFragmentBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

const val RESUME_LESSON_FRAGMENT_INTERNAL_PROFILE_ID_KEY =
  "ResumeExplorationFragmentPresenter.resume_exploration_fragment_internal_profile_id"
const val RESUME_LESSON_FRAGMENT_TOPIC_ID_KEY =
  "ResumeExplorationFragmentPresenter.resume_exploration_fragment_topic_id"
const val RESUME_LESSON_FRAGMENT_STORY_ID_KEY =
  "ResumeExplorationFragmentPresenter.resume_exploration_fragment_story_id"
const val RESUME_LESSON_FRAGMENT_EXPLORATION_ID_KEY =
  "ResumeExplorationFragmentPresenter.resume_Lesson_fragment_exploration_id"
const val RESUME_LESSON_FRAGMENT_BACKFLOW_SCREEN_KEY =
  "ResumeLessonFragmentPresenter.resume_lesson_fragment_backflow_screen"

class ResumeLessonFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ResumeLessonViewModel>,
  private val topicController: TopicController,
  private val explorationDataController: ExplorationDataController,
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val oppiaLogger: OppiaLogger
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  private lateinit var binding: ResumeLessonFragmentBinding
  private val resumeLessonViewModel = getResumeLessonViewModel()
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String

  private val explorationCheckpointResultLiveData:
    LiveData<AsyncResult<ExplorationCheckpoint>> by lazy {
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        explorationId
      ).toLiveData()
    }

  private val chapterSummaryResultLiveData: LiveData<AsyncResult<ChapterSummary>> by lazy {
    topicController.getChapter(topicId, storyId, explorationId).toLiveData()
  }

  private val explorationCheckpointLiveData: LiveData<ExplorationCheckpoint> by lazy {
    getExplorationCheckpoint()
  }

  private val chapterSummaryLiveData: LiveData<ChapterSummary> by lazy { getChapterSummary() }

  fun handleOnCreate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ): View? {

    binding = ResumeLessonFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    this.profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    this.topicId = topicId
    this.storyId = storyId
    this.explorationId = explorationId

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = resumeLessonViewModel
    }

    subscribeToChapterSummary()
    subscribeToExplorationCheckpoint()

    binding.resumeLessonContinueButton.setOnClickListener {
      playExploration(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        resumeLessonViewModel.explorationCheckpoint.get()!!,
        backflowScreen
      )
    }

    binding.resumeLessonStartOverButton.setOnClickListener {
      playExploration(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        ExplorationCheckpoint.getDefaultInstance(),
        backflowScreen
      )
    }

    return binding.root
  }

  private fun subscribeToExplorationCheckpoint() {
    explorationCheckpointLiveData.observe(
      fragment,
      Observer<ExplorationCheckpoint> { checkpoint ->
        resumeLessonViewModel.explorationCheckpoint.set(checkpoint)
      }
    )
  }

  private fun subscribeToChapterSummary() {
    chapterSummaryLiveData.observe(
      fragment,
      Observer<ChapterSummary> { chapterSummary ->
        resumeLessonViewModel.chapterSummary.set(chapterSummary)
        updateChapterDescription()
      }
    )
  }

  private fun updateChapterDescription() {
    binding.resumeLessonChapterDescriptionTextView.text = htmlParserFactory.create(
      resourceBucketName,
      resumeLessonViewModel.entityType,
      explorationId,
      imageCenterAlign = true
    ).parseOppiaHtml(
      resumeLessonViewModel.chapterSummary.get()!!.summary,
      binding.resumeLessonChapterDescriptionTextView
    )
  }

  private fun getResumeLessonViewModel(): ResumeLessonViewModel {
    return viewModelProvider.getForFragment(fragment, ResumeLessonViewModel::class.java)
  }

  private fun getExplorationCheckpoint(): LiveData<ExplorationCheckpoint> {
    return Transformations.map(
      explorationCheckpointResultLiveData,
      ::processExplorationCheckpointResult
    )
  }

  private fun getChapterSummary(): LiveData<ChapterSummary> {
    return Transformations.map(chapterSummaryResultLiveData, ::processChapterSummaryResult)
  }

  private fun processExplorationCheckpointResult(
    explorationCheckpointResult: AsyncResult<ExplorationCheckpoint>
  ): ExplorationCheckpoint {
    if (explorationCheckpointResult.isFailure()) {
      oppiaLogger.e(
        "ResumeLessonFragment",
        "Failed to retrieve exploration checkpoint for the profileId ${profileId.internalId}" +
          "and explorationId $explorationId",
        explorationCheckpointResult.getErrorOrNull()
      )
    }
    return explorationCheckpointResult.getOrDefault(ExplorationCheckpoint.getDefaultInstance())
  }

  private fun processChapterSummaryResult(
    chapterSummaryResult: AsyncResult<ChapterSummary>
  ): ChapterSummary {
    if (chapterSummaryResult.isFailure()) {
      oppiaLogger.e(
        "ResumeLessonFragment",
        "Failed to retrieve chapter summary for the explorationId $explorationId: ",
        chapterSummaryResult.getErrorOrNull()
      )
    }
    return chapterSummaryResult.getOrDefault(ChapterSummary.getDefaultInstance())
  }

  private fun playExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    explorationCheckpoint: ExplorationCheckpoint,
    backflowScreen: Int?
  ) {
    explorationDataController.startPlayingExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      // shouldSavePartialProgress is set to true by default because stating lesson from
      // ResumeLessonActivity implies that learner has not completed the lesson.
      shouldSavePartialProgress = true,
      explorationCheckpoint
    ).observe(
      fragment,
      Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> oppiaLogger.d("ResumeLessonFragment", "Loading exploration")
          result.isFailure() -> oppiaLogger.e(
            "ResumeLessonFragment",
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            oppiaLogger.d("ResumeLessonFragment", "Successfully loaded exploration")
            routeToExplorationListener.routeToExploration(
              internalProfileId,
              topicId,
              storyId,
              explorationId,
              backflowScreen,
              // Checkpointing is enabled be default because stating lesson from
              // ResumeLessonActivity implies that learner has not completed the lesson.
              isCheckpointingEnabled = true
            )
          }
        }
      }
    )
  }
}

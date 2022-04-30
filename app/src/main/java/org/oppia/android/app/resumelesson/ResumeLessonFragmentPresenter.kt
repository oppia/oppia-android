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
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ResumeLessonFragmentBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** The presenter for [ResumeLessonFragment]. */
class ResumeLessonFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ResumeLessonViewModel>,
  private val topicController: TopicController,
  private val explorationDataController: ExplorationDataController,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val oppiaLogger: OppiaLogger
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  private lateinit var binding: ResumeLessonFragmentBinding
  private val resumeLessonViewModel = getResumeLessonViewModel()
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String

  private val chapterSummaryResultLiveData: LiveData<AsyncResult<ChapterSummary>> by lazy {
    topicController.retrieveChapter(topicId, storyId, explorationId).toLiveData()
  }

  private val chapterSummaryLiveData: LiveData<ChapterSummary> by lazy { getChapterSummary() }

  /** Handles onCreateView() method of the [ResumeLessonFragment]. */
  fun handleOnCreate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    explorationCheckpoint: ExplorationCheckpoint
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

    resumeLessonViewModel.explorationCheckpoint.set(explorationCheckpoint)
    subscribeToChapterSummary()

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
      binding.resumeLessonChapterDescriptionTextView,
      displayLocale = appLanguageResourceHandler.getDisplayLocale()
    )
  }

  private fun getResumeLessonViewModel(): ResumeLessonViewModel {
    return viewModelProvider.getForFragment(fragment, ResumeLessonViewModel::class.java)
  }

  private fun getChapterSummary(): LiveData<ChapterSummary> {
    return Transformations.map(chapterSummaryResultLiveData, ::processChapterSummaryResult)
  }

  private fun processChapterSummaryResult(
    chapterSummaryResult: AsyncResult<ChapterSummary>
  ): ChapterSummary {
    return when (chapterSummaryResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ResumeLessonFragment",
          "Failed to retrieve chapter summary for the explorationId $explorationId: ",
          chapterSummaryResult.error
        )
        ChapterSummary.getDefaultInstance()
      }
      is AsyncResult.Pending -> ChapterSummary.getDefaultInstance()
      is AsyncResult.Success -> chapterSummaryResult.value
    }
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
      // ResumeLessonFragment implies that learner has not completed the lesson.
      shouldSavePartialProgress = true,
      explorationCheckpoint
    ).toLiveData().observe(
      fragment,
      Observer<AsyncResult<Any?>> { result ->
        when (result) {
          is AsyncResult.Pending -> oppiaLogger.d("ResumeLessonFragment", "Loading exploration")
          is AsyncResult.Failure ->
            oppiaLogger.e("ResumeLessonFragment", "Failed to load exploration", result.error)
          is AsyncResult.Success -> {
            oppiaLogger.d("ResumeLessonFragment", "Successfully loaded exploration")
            routeToExplorationListener.routeToExploration(
              internalProfileId,
              topicId,
              storyId,
              explorationId,
              backflowScreen,
              // Checkpointing is enabled be default because stating lesson from
              // ResumeLessonFragment implies that learner has not completed the lesson.
              isCheckpointingEnabled = true
            )
          }
        }
      }
    )
  }
}

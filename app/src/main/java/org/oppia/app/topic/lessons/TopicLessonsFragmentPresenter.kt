package org.oppia.app.topic.lessons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.TopicLessonsFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StorySummary
import org.oppia.app.model.Topic
import org.oppia.app.player.exploration.BackflowScreenEnum
import org.oppia.app.topic.RouteToStoryListener
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicLessonsFragment]. */
@FragmentScope
class TopicLessonsFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val explorationDataController: ExplorationDataController,
  private val topicController: TopicController
) : StorySummarySelector, ChapterSummarySelector {
  private val routeToExplorationListener = activity as RouteToExplorationListener
  private val routeToStoryListener = activity as RouteToStoryListener

  private var currentExpandedChapterListIndex: Int? = null

  private lateinit var binding: TopicLessonsFragmentBinding
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String

  private lateinit var expandedChapterListIndexListener: ExpandedChapterListIndexListener

  private val itemList: MutableList<TopicLessonsItemViewModel> = ArrayList()

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    currentExpandedChapterListIndex: Int?,
    expandedChapterListIndexListener: ExpandedChapterListIndexListener,
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): View? {
    this.internalProfileId = internalProfileId
    this.topicId = topicId
    this.storyId = storyId
    this.currentExpandedChapterListIndex = currentExpandedChapterListIndex
    this.expandedChapterListIndexListener = expandedChapterListIndexListener
    binding = TopicLessonsFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.lifecycleOwner = fragment
    }
    subscribeToTopicLiveData()
    return binding.root
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(ProfileId.newBuilder().setInternalId(internalProfileId).build(), topicId)
  }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> {
      if (it.storyList.isNotEmpty()) {
        it.storyList!!.forEach { storySummary ->
          if (storySummary.storyId == storyId) {
            val index = it.storyList.indexOf(storySummary)
            currentExpandedChapterListIndex = index + 1
          }
        }
        itemList.add(TopicLessonsTitleViewModel())
        for (storySummary in it.storyList) {
          itemList.add(StorySummaryViewModel(storySummary, fragment as StorySummarySelector))
        }
        val storySummaryAdapter =
          StorySummaryAdapter(
            itemList,
            this as ChapterSummarySelector,
            expandedChapterListIndexListener,
            currentExpandedChapterListIndex
          )
        binding.storySummaryRecyclerView.apply {
          adapter = storySummaryAdapter
        }
        if (storyId.isNotEmpty())
          binding.storySummaryRecyclerView.layoutManager!!.scrollToPosition(currentExpandedChapterListIndex!!)
      }
    })
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicLessonsFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  override fun selectStorySummary(storySummary: StorySummary) {
    routeToStoryListener.routeToStory(internalProfileId, topicId, storySummary.storyId)
  }

  override fun selectChapterSummary(storyId: String, chapterSummary: ChapterSummary) {
    playExploration(internalProfileId, topicId, storyId, chapterSummary.explorationId, BackflowScreenEnum.BACKFLOW_SCREEN_LESSONS.value)
  }

  private fun playExploration(internalProfileId: Int, topicId: String, storyId: String, explorationId: String, backflowScreen: Int) {
    explorationDataController.startPlayingExploration(
      explorationId
    ).observe(fragment, Observer<AsyncResult<Any?>> { result ->
      when {
        result.isPending() -> logger.d("TopicLessonsFragment", "Loading exploration")
        result.isFailure() -> logger.e("TopicLessonsFragment", "Failed to load exploration", result.getErrorOrNull()!!)
        else -> {
          logger.d("TopicLessonsFragment", "Successfully loaded exploration")
          routeToExplorationListener.routeToExploration(internalProfileId, topicId, storyId, explorationId, backflowScreen)
        }
      }
    })
  }

  fun storySummaryClicked(storySummary: StorySummary) {
    routeToStoryListener.routeToStory(internalProfileId, topicId, storySummary.storyId)
  }
}

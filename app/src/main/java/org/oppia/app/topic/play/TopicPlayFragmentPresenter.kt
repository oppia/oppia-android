package org.oppia.app.topic.play

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.TopicPlayFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.StorySummary
import org.oppia.app.model.Topic
import org.oppia.app.topic.RouteToStoryListener
import org.oppia.app.topic.TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicPlayFragment]. */
@FragmentScope
class TopicPlayFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val explorationDataController: ExplorationDataController,
  private val topicController: TopicController
) : StorySummarySelector, ChapterSummarySelector {
  private val routeToExplorationListener = activity as RouteToExplorationListener
  private val routeToStoryListener = activity as RouteToStoryListener

  private var currentExpandedChapterListIndex: Int? = null

  private lateinit var binding: TopicPlayFragmentBinding
  private lateinit var topicId: String

  private lateinit var expandedChapterListIndexListener: ExpandedChapterListIndexListener

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    currentExpandedChapterListIndex: Int?,
    expandedChapterListIndexListener: ExpandedChapterListIndexListener
  ): View? {
    this.currentExpandedChapterListIndex = currentExpandedChapterListIndex
    this.expandedChapterListIndexListener = expandedChapterListIndexListener

    binding = TopicPlayFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.lifecycleOwner = fragment
    }
    subscribeToTopicLiveData()
    return binding.root
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicId = fragment.arguments?.getString(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY) ?: ""
    topicController.getTopic(topicId)
  }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> {
      val storySummaryAdapter =
        StorySummaryAdapter(
          it.storyList,
          this as ChapterSummarySelector,
          this as StorySummarySelector,
          expandedChapterListIndexListener,
          currentExpandedChapterListIndex
        )
      binding.storySummaryRecyclerView.apply {
        adapter = storySummaryAdapter
      }
    })
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicPlayFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  override fun selectStorySummary(storySummary: StorySummary) {
    routeToStoryListener.routeToStory(storySummary.storyId)
  }

  override fun selectChapterSummary(chapterSummary: ChapterSummary) {
    playExploration(chapterSummary.explorationId)
  }

  private fun playExploration(explorationId: String) {
    explorationDataController.startPlayingExploration(
      explorationId
    ).observe(fragment, Observer<AsyncResult<Any?>> { result ->
      when {
        result.isPending() -> logger.d("TopicPlayFragment", "Loading exploration")
        result.isFailure() -> logger.e("TopicPlayFragment", "Failed to load exploration", result.getErrorOrNull()!!)
        else -> {
          logger.d("TopicPlayFragment", "Successfully loaded exploration")
          routeToExplorationListener.routeToExploration(explorationId)
        }
      }
    })
  }
}

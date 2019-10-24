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
import org.oppia.app.model.StorySummary
import org.oppia.app.model.Topic
import org.oppia.app.topic.RouteToStoryListener
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TEST_TOPIC_ID_0
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
  private val topicController: TopicController,
  private val viewModelProvider: ViewModelProvider<TopicPlayViewModel>
) : StorySummarySelector {

  private val routeToStoryListener = activity as RouteToStoryListener

  private lateinit var binding: TopicPlayFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = TopicPlayFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.viewModel = getTopicPlayViewModel()
      it.lifecycleOwner = fragment
    }
    subscribeToTopicLiveData()
    return binding.root
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  // TODO(#135): Get this topic-id or get storyList from [StoryFragment].
  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(TEST_TOPIC_ID_0)
  }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> {
      val storySummaryAdapter = StorySummaryAdapter(it.storyList)
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

  private fun getTopicPlayViewModel(): TopicPlayViewModel {
    return viewModelProvider.getForFragment(fragment, TopicPlayViewModel::class.java)
  }

  override fun selectedStorySummary(storySummary: StorySummary) {
    routeToStoryListener.routeToStory(storySummary.storyId)
  }
}

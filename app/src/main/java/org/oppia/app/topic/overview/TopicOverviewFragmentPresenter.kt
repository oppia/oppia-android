package org.oppia.app.topic.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.TopicOverviewFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.topic.RouteToTopicPlayListener
import org.oppia.app.topic.TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicOverviewFragment]. */
@FragmentScope
class TopicOverviewFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicOverviewViewModel>,
  private val logger: Logger,
  private val topicController: TopicController
) {
  private val routeToTopicPlayListener = activity as RouteToTopicPlayListener

  private val topicOverviewViewModel = getTopicOverviewViewModel()
  private lateinit var topicId: String

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicOverviewFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    subscribeToTopicLiveData()
    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      it.viewModel = topicOverviewViewModel
    }
    return binding.root
  }

  fun seeMoreClicked(v: View) {
    routeToTopicPlayListener.routeToTopicPlayFragment()
  }

  private fun getTopicOverviewViewModel(): TopicOverviewViewModel {
    return viewModelProvider.getForFragment(fragment, TopicOverviewViewModel::class.java)
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> { result ->
      topicOverviewViewModel.topic.set(result)
    })
  }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicId =
      if (fragment.arguments != null && fragment.arguments!!.getString(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY) != null) fragment.arguments!!.getString(
        TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
      ) else ""
    topicController.getTopic(topicId)
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicOverviewFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }
}

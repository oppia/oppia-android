package org.oppia.app.topic.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.databinding.TopicInfoFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** The presenter for [TopicInfoFragment]. */
@FragmentScope
class TopicInfoFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicInfoViewModel>,
  private val logger: Logger,
  private val topicController: TopicController,
  private val htmlParserFactory: HtmlParser.Factory
) {
  private val topicInfoViewModel = getTopicInfoViewModel()
  private lateinit var topicId: String
  private val htmlParser: HtmlParser by lazy {
    htmlParserFactory.create(/* entityType= */"topic", topicId, /* imageCenterAlign= */ true)
  }

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicInfoFragment."
    }
    val binding = TopicInfoFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    subscribeToTopicLiveData()
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = topicInfoViewModel
    }
    return binding.root
  }

  private fun getTopicInfoViewModel(): TopicInfoViewModel {
    return viewModelProvider.getForFragment(fragment, TopicInfoViewModel::class.java)
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> { topic ->
      topicInfoViewModel.topic.set(topic)
      topicInfoViewModel.topicDescription.set(
        htmlParser.parseOppiaHtml(
          topic.description,
          fragment.requireView().findViewById(R.id.topic_description_text_view)
        )
      )
    })
  }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(topicId)
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicInfoFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }
}

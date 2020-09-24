package org.oppia.android.app.topic.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.databinding.TopicInfoFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.HtmlParser
import org.oppia.android.app.R
import org.oppia.android.app.databinding.TopicInfoFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.HtmlParser
import javax.inject.Inject

/** The presenter for [TopicInfoFragment]. */
@FragmentScope
class TopicInfoFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicInfoViewModel>,
  private val logger: ConsoleLogger,
  private val topicController: TopicController,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String
) {
  private lateinit var binding: TopicInfoFragmentBinding
  private val topicInfoViewModel = getTopicInfoViewModel()
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private val htmlParser: HtmlParser by lazy {
    htmlParserFactory
      .create(
        resourceBucketName,
        /* entityType= */ "topic",
        topicId,
        /* imageCenterAlign= */ true
      )
  }

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String
  ): View? {
    this.internalProfileId = internalProfileId
    this.topicId = topicId
    binding = TopicInfoFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
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
    topicLiveData.observe(
      fragment,
      Observer<Topic> { topic ->
        topicInfoViewModel.topic.set(topic)
        topicInfoViewModel.topicDescription.set(topic.description)
        topicInfoViewModel.calculateTopicSizeWithUnit()
        controlSeeMoreTextVisibility()
      }
    )
  }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId
    ).toLiveData()
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

  private fun controlSeeMoreTextVisibility() {
    val minimumNumberOfLines = fragment.resources.getInteger(R.integer.topic_description_collapsed)
    binding.topicDescriptionTextView.post {
      if (binding.topicDescriptionTextView.lineCount > minimumNumberOfLines) {
        getTopicInfoViewModel().isDescriptionExpanded.set(false)
        getTopicInfoViewModel().isSeeMoreVisible.set(true)
      } else {
        getTopicInfoViewModel().isDescriptionExpanded.set(true)
        getTopicInfoViewModel().isSeeMoreVisible.set(false)
      }
    }
  }
}

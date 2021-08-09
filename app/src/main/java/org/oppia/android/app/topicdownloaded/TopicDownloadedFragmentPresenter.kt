package org.oppia.android.app.topicdownloaded

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.TopicDownloadedFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [TopicDownloadedFragment]. */
@FragmentScope
class TopicDownloadedFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicDownloadedViewModel>,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger
) {

  private val topicDownloadedViewModel = getTopicDownloadedViewModel()
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String

  /** Bind TopicDownloadedFragmentBinding with the TopicDownloadedFragment */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String
  ): View? {
    val binding = TopicDownloadedFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = topicDownloadedViewModel
    }
    this.topicId = topicId
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    subscribeToTopicLiveData()
    topicDownloadedViewModel.internalProfileId = internalProfileId
    topicDownloadedViewModel.topicId = topicId
    return binding.root
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopic() }

  private fun getTopic(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      oppiaLogger.e(
        "TopicDownloadedFragment",
        "Failed to retrieve topic",
        topic.getErrorOrNull()!!
      )
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(profileId, topicId = topicId).toLiveData()
  }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(
      fragment,
      Observer { result ->
        topicDownloadedViewModel.topicName.set(result.name)
      }
    )
  }

  private fun getTopicDownloadedViewModel(): TopicDownloadedViewModel {
    return viewModelProvider.getForFragment(fragment, TopicDownloadedViewModel::class.java)
  }
}

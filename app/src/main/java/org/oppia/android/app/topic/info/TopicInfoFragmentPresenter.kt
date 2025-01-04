package org.oppia.android.app.topic.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.TopicInfoFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** The presenter for [TopicInfoFragment]. */
@FragmentScope
class TopicInfoFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModel: TopicInfoViewModel,
  private val oppiaLogger: OppiaLogger,
  private val topicController: TopicController,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String
) {
  private lateinit var binding: TopicInfoFragmentBinding
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    topicId: String
  ): View? {
    this.profileId = profileId
    this.topicId = topicId
    binding = TopicInfoFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    subscribeToTopicLiveData()
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private val topicLiveData: LiveData<EphemeralTopic> by lazy { getTopicList() }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(
      fragment,
      { ephemeralTopic ->
        viewModel.setTopic(ephemeralTopic)
        viewModel.calculateTopicSizeWithUnit()
        controlSeeMoreTextVisibility()
      }
    )
  }

  private val topicResultLiveData: LiveData<AsyncResult<EphemeralTopic>> by lazy {
    topicController.getTopic(profileId, topicId).toLiveData()
  }

  private fun getTopicList(): LiveData<EphemeralTopic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(ephemeralResult: AsyncResult<EphemeralTopic>): EphemeralTopic {
    return when (ephemeralResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("TopicInfoFragment", "Failed to retrieve topic", ephemeralResult.error)
        EphemeralTopic.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralTopic.getDefaultInstance()
      is AsyncResult.Success -> ephemeralResult.value
    }
  }

  private fun controlSeeMoreTextVisibility() {
    val minimumNumberOfLines = fragment.resources.getInteger(R.integer.topic_description_collapsed)
    binding.topicDescriptionTextView.post {
      if (binding.topicDescriptionTextView.lineCount > minimumNumberOfLines) {
        viewModel.isDescriptionExpanded.set(false)
        viewModel.isSeeMoreVisible.set(true)
      } else {
        viewModel.isDescriptionExpanded.set(true)
        viewModel.isSeeMoreVisible.set(false)
      }
    }
  }
}

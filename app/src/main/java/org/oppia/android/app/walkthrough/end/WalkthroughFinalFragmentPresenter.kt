package org.oppia.android.app.walkthrough.end

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.walkthrough.WalkthroughActivity
import org.oppia.android.databinding.WalkthroughFinalFragmentBinding
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** The presenter for [WalkthroughFinalFragment]. */
@FragmentScope
class WalkthroughFinalFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: ConsoleLogger,
  private val topicController: TopicController
) : WalkthroughEndPageChanger {
  private lateinit var binding: WalkthroughFinalFragmentBinding
  private lateinit var walkthroughFinalViewModel: WalkthroughFinalViewModel
  private lateinit var topicId: String
  private lateinit var profileId: ProfileId
  private lateinit var topicName: String

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, topicId: String): View? {
    binding =
      WalkthroughFinalFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    this.topicId = topicId
    val internalProfileId = activity.intent.getIntExtra(
      WalkthroughActivity.WALKTHROUGH_ACTIVITY_INTERNAL_PROFILE_ID_KEY,
      -1
    )
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    walkthroughFinalViewModel = WalkthroughFinalViewModel()

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      it.viewModel = walkthroughFinalViewModel
    }
    subscribeToTopicLiveData()
    return binding.root
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopic() }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(
      activity,
      Observer { result ->
        topicName = result.name
        setTopicName()
      }
    )
  }

  private fun setTopicName() {
    if (::walkthroughFinalViewModel.isInitialized && ::topicName.isInitialized) {
      walkthroughFinalViewModel.topicTitle.set(
        activity.getString(
          R.string.are_you_interested,
          topicName
        )
      )
    }
  }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(profileId, topicId = topicId)
  }

  private fun getTopic(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e(
        "WalkthroughFinalFragment",
        "Failed to retrieve topic",
        topic.getErrorOrNull()!!
      )
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  override fun goBack() {
    activity.onBackPressed()
  }
}

package org.oppia.app.topic.revision

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.databinding.TopicRevisionFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Subtopic
import org.oppia.app.model.Topic
import org.oppia.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.app.topic.RouteToRevisionCardListener
import org.oppia.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicRevisionFragment]. */
@FragmentScope
class TopicRevisionFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val topicController: TopicController
) : RevisionSubtopicSelector {
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private val routeToReviewListener = activity as RouteToRevisionCardListener

  private lateinit var revisionAdapter: RevisionSubtopicAdapter

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    internalProfileId = fragment.arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicRevisionFragment."
    }
    val binding = TopicRevisionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    revisionAdapter = RevisionSubtopicAdapter(this)

    binding.revisionRecyclerView.apply {
      adapter = revisionAdapter
      // https://stackoverflow.com/a/50075019/3689782
      val spanCount = if( fragment.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE )  3 else  2
      layoutManager = GridLayoutManager(context,spanCount)
    }
    binding.let {
      it.lifecycleOwner = fragment
    }
    subscribeToTopicLiveData()
    return binding.root
  }

  override fun onTopicRevisionSummaryClicked(subtopic: Subtopic) {
    routeToReviewListener.routeToRevisionCard(topicId, subtopic.subtopicId)
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(ProfileId.newBuilder().setInternalId(internalProfileId).build(), topicId)
  }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> { result ->
      revisionAdapter.setRevisionList(result.subtopicList)
    })
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicRevisionFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }
}

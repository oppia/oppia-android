package org.oppia.app.topic.revision

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
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.Topic
import org.oppia.app.topic.RouteToConceptCardListener
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
) : RevisionSkillSelector {
  private lateinit var topicId: String
  private val routeToRevisionListener = activity as RouteToConceptCardListener

  private lateinit var revisionSkillSelectionAdapter: RevisionSkillSelectionAdapter

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicRevisionFragment."
    }
    val binding = TopicRevisionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    revisionSkillSelectionAdapter = RevisionSkillSelectionAdapter(this)
    binding.revisionSkillRecyclerView.apply {
      adapter = revisionSkillSelectionAdapter
      // https://stackoverflow.com/a/50075019/3689782
      layoutManager = GridLayoutManager(context, /* spanCount= */ 2)
    }
    binding.let {
      it.lifecycleOwner = fragment
    }
    subscribeToTopicLiveData()
    return binding.root
  }

  override fun onTopicRevisionSummaryClicked(skillSummary: SkillSummary) {
    routeToRevisionListener.routeToConceptCard(skillSummary.skillId)
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(topicId)
  }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> { result ->
      revisionSkillSelectionAdapter.setSkillList(result.skillList)
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

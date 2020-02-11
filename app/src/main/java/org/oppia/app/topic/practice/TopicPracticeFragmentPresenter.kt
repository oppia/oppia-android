package org.oppia.app.topic.practice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.TopicPracticeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.topic.RouteToQuestionPlayerListener
import org.oppia.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicPracticeFragment]. */
@FragmentScope
class TopicPracticeFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val topicController: TopicController,
  private val viewModelProvider: ViewModelProvider<TopicPracticeViewModel>
) : SubtopicSkillsSelector {
  lateinit var selectedSubtopicIdList: ArrayList<String>
  private lateinit var topicId: String
  private val routeToQuestionPlayerListener = activity as RouteToQuestionPlayerListener
  private lateinit var subtopicSkillSelectionAdapter: SubtopicSkillSelectionAdapter

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, skillList: ArrayList<String>): View? {
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicPracticeFragment."
    }
    selectedSubtopicIdList = skillList
    val binding = TopicPracticeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    subtopicSkillSelectionAdapter = SubtopicSkillSelectionAdapter(this)
    binding.skillRecyclerView.isNestedScrollingEnabled = false
    binding.skillRecyclerView.apply {
      adapter = subtopicSkillSelectionAdapter
    }
    binding.let {
      it.viewModel = getTopicPracticeViewModel()
      it.lifecycleOwner = fragment
    }
    subscribeToTopicLiveData()
    return binding.root
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(topicId)
  }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> { result ->
      subtopicSkillSelectionAdapter.setSubtopicSkillList(result.subtopicList)
      subtopicSkillSelectionAdapter.setSelectedSubtopicSkillList(selectedSubtopicIdList)
    })
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicPracticeFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private fun getTopicPracticeViewModel(): TopicPracticeViewModel {
    return viewModelProvider.getForFragment(fragment, TopicPracticeViewModel::class.java)
  }

  override fun subtopicSkillsSelected(
    subtopicId: String,
    skillIdList: MutableList<String>
  ) {
    if (!selectedSubtopicIdList.contains(subtopicId)) {
      selectedSubtopicIdList.add(subtopicId)
    }
    getTopicPracticeViewModel().notifySelectedSubtopicList(selectedSubtopicIdList)
  }

  override fun subtopicSkillsUnselected(
    subtopicId: String,
    skillIdList: MutableList<String>
  ) {
    if (selectedSubtopicIdList.contains(subtopicId)) {
      selectedSubtopicIdList.remove(subtopicId)
    }
    getTopicPracticeViewModel().notifySelectedSubtopicList(selectedSubtopicIdList)
  }

  internal fun onStartButtonClicked() {
    routeToQuestionPlayerListener.routeToQuestionPlayer(selectedSubtopicIdList)
  }
}

package org.oppia.app.topic.train

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.TopicTrainFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.topic.RouteToQuestionPlayerListener
import org.oppia.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicTrainFragment]. */
@FragmentScope
class TopicTrainFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val topicController: TopicController,
  private val viewModelProvider: ViewModelProvider<TopicTrainViewModel>
) : SkillSelector {
  lateinit var selectedSkillIdList: ArrayList<String>
  private lateinit var topicId: String
  private val routeToQuestionPlayerListener = activity as RouteToQuestionPlayerListener
  private lateinit var skillSelectionAdapter: SkillSelectionAdapter

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, skillList: ArrayList<String>): View? {
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicTrainFragment."
    }
    selectedSkillIdList = skillList
    val binding = TopicTrainFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    skillSelectionAdapter = SkillSelectionAdapter(this)
    binding.skillRecyclerView.isNestedScrollingEnabled = false
    binding.skillRecyclerView.apply {
      adapter = skillSelectionAdapter
    }
    binding.let {
      it.viewModel = getTopicTrainViewModel()
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
      skillSelectionAdapter.setSkillList(result.skillList)
      skillSelectionAdapter.setSelectedSkillList(selectedSkillIdList)
    })
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicTrainFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private fun getTopicTrainViewModel(): TopicTrainViewModel {
    return viewModelProvider.getForFragment(fragment, TopicTrainViewModel::class.java)
  }

  override fun skillSelected(skillId: String) {
    if (!selectedSkillIdList.contains(skillId)) {
      selectedSkillIdList.add(skillId)
    }
    getTopicTrainViewModel().notifySelectedSkillList(selectedSkillIdList)
  }

  override fun skillUnselected(skillId: String) {
    if (selectedSkillIdList.contains(skillId)) {
      selectedSkillIdList.remove(skillId)
    }
    getTopicTrainViewModel().notifySelectedSkillList(selectedSkillIdList)
  }

  internal fun onStartButtonClicked() {
    routeToQuestionPlayerListener.routeToQuestionPlayer(selectedSkillIdList)
  }
}

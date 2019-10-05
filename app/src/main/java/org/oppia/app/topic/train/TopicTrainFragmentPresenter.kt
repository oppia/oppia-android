package org.oppia.app.topic.train

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.application.ApplicationContext
import org.oppia.app.databinding.TopicTrainFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.Topic
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicTrainFragment]. */
@FragmentScope
class TopicTrainFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment,
  private val logger: Logger,
  private val topicController: TopicController,
  private val viewModelProvider: ViewModelProvider<TopicTrainViewModel>
) : SkillSelector {
  val selectedSkillList = ArrayList<SkillSummary>()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicTrainFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.skillRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
    }
    binding.let {
      it.viewModel = getTopicTrainViewModel()
      it.lifecycleOwner = fragment
    }
    getSkillList(binding)
    return binding.root
  }

  // TODO(#135): Get this topic-id or get skillList from from [TopicFragment].
  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(TEST_TOPIC_ID_0)
  }

  private fun getSkillList(binding: TopicTrainFragmentBinding) {
    topicLiveData.observe(fragment, Observer<Topic> { result ->
      val skillAdapter = SkillSelectionAdapter(result.skillList, this)
      binding.skillRecyclerView.adapter = skillAdapter
    })
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicTrainFragmentPresenter", "Failed to retrieve ephemeral state ${topic.getErrorOrNull()}")
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private fun getTopicTrainViewModel(): TopicTrainViewModel {
    return viewModelProvider.getForFragment(fragment, TopicTrainViewModel::class.java)
  }

  override fun skillSelected(skill: SkillSummary) {
    selectedSkillList.add(skill)
    getTopicTrainViewModel().selectedSkillList(selectedSkillList)
  }

  override fun skillUnselected(skill: SkillSummary) {
    selectedSkillList.remove(skill)
    getTopicTrainViewModel().selectedSkillList(selectedSkillList)
  }

  fun startButtonClicked(v: View) {
    val skillList = selectedSkillList
    val skillIdList = ArrayList<String>()
    for (skill in skillList) {
      skillIdList.add(skill.skillId)
    }
    val questionPlayerIntent = Intent(context, QuestionPlayerActivity::class.java)
    questionPlayerIntent.putStringArrayListExtra("SKILL_ID_LIST", skillIdList)
    context.startActivity(questionPlayerIntent)
  }
}

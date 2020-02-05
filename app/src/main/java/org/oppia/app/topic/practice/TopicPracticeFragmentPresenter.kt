package org.oppia.app.topic.practice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import kotlinx.android.synthetic.main.topic_practice_skill_view.view.*
import org.oppia.app.databinding.TopicPracticeFragmentBinding
import org.oppia.app.databinding.TopicPracticeSkillViewBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.Topic
import org.oppia.app.recyclerview.BindableAdapter
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
  private val topicController: TopicController,
  private val logger: Logger,
  private val viewModelProvider: ViewModelProvider<TopicPracticeViewModel>
) : SkillSelector {
  lateinit var selectedSkillIdList: ArrayList<String>
  private lateinit var topicId: String
  private val routeToQuestionPlayerListener = activity as RouteToQuestionPlayerListener
  private lateinit var  skillSelector: SkillSelector
  private lateinit var skill: SkillSummary

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, skillList: ArrayList<String>): View? {
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicPracticeFragment."
    }
    selectedSkillIdList = skillList
    val binding = TopicPracticeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.skillRecyclerView.isNestedScrollingEnabled = false

    binding.skillRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    binding.let {
      it.viewModel = getTopicPracticeViewModel()
      it.lifecycleOwner = fragment
    }
    subscribeToTopicLiveData()

    return binding.root
  }
  private fun createRecyclerViewAdapter(): BindableAdapter<TopicPracticeViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicPracticeViewModel>()
      .registerViewBinder(
        inflateView = { parent ->
          TopicPracticeSkillViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<TopicPracticeSkillViewBinding>(view)!!
          binding.root.skill_check_box.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
              skillSelector.skillSelected(skill.skillId)
            } else {
              skillSelector.skillUnselected(skill.skillId)
            }
          }
        }
      )
      .build()
  }
  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(topicId)
  }

  private fun subscribeToTopicLiveData() {
    topicLiveData.observe(fragment, Observer<Topic> { result ->
//      skillSelectionAdapter.setSkillList(result.skillList)
//      skillSelectionAdapter.setSelectedSkillList(selectedSkillIdList)
      getTopicPracticeViewModel().setSkillList(result.skillList)
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

  override fun skillSelected(skillId: String) {
    if (!selectedSkillIdList.contains(skillId)) {
      selectedSkillIdList.add(skillId)
    }
    getTopicPracticeViewModel().notifySelectedSkillList(selectedSkillIdList)
  }

  override fun skillUnselected(skillId: String) {
    if (selectedSkillIdList.contains(skillId)) {
      selectedSkillIdList.remove(skillId)
    }
    getTopicPracticeViewModel().notifySelectedSkillList(selectedSkillIdList)
  }

  internal fun onStartButtonClicked() {
    routeToQuestionPlayerListener.routeToQuestionPlayer(selectedSkillIdList)
  }
}

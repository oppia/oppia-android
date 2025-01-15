package org.oppia.android.app.topic.practice

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.practice.practiceitemviewmodel.TopicPracticeFooterViewModel
import org.oppia.android.app.topic.practice.practiceitemviewmodel.TopicPracticeHeaderViewModel
import org.oppia.android.app.topic.practice.practiceitemviewmodel.TopicPracticeItemViewModel
import org.oppia.android.app.topic.practice.practiceitemviewmodel.TopicPracticeSubtopicViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** [ObservableViewModel] for [TopicPracticeFragment]. */
@FragmentScope
class TopicPracticeViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val topicController: TopicController,
  private val translationController: TranslationController
) : ObservableViewModel() {
  private val itemViewModelList: MutableList<TopicPracticeItemViewModel> = ArrayList()
  private lateinit var topicId: String
  private lateinit var profileId: ProfileId

  private val topicResultLiveData: LiveData<AsyncResult<EphemeralTopic>> by lazy {
    topicController.getTopic(profileId, topicId).toLiveData()
  }

  private val topicLiveData: LiveData<EphemeralTopic> by lazy { getTopicList() }

  private fun getTopicList(): LiveData<EphemeralTopic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  val topicPracticeSkillLiveData: LiveData<List<TopicPracticeItemViewModel>> by lazy {
    Transformations.map(topicLiveData, ::processTopicPracticeSkillList)
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  fun setInternalProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  private fun processTopicResult(ephemeralResult: AsyncResult<EphemeralTopic>): EphemeralTopic {
    return when (ephemeralResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("TopicPracticeFragment", "Failed to retrieve topic", ephemeralResult.error)
        EphemeralTopic.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralTopic.getDefaultInstance()
      is AsyncResult.Success -> ephemeralResult.value
    }
  }

  private fun processTopicPracticeSkillList(
    ephemeralTopic: EphemeralTopic
  ): List<TopicPracticeItemViewModel> {
    itemViewModelList.clear()
    itemViewModelList.add(TopicPracticeHeaderViewModel() as TopicPracticeItemViewModel)

    itemViewModelList.addAll(
      ephemeralTopic.subtopicsList.map { ephemeralSubtopic ->
        TopicPracticeSubtopicViewModel(ephemeralSubtopic, translationController)
      }
    )

    itemViewModelList.add(TopicPracticeFooterViewModel() as TopicPracticeItemViewModel)

    return itemViewModelList
  }
}

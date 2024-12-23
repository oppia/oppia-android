package org.oppia.android.app.topic.lessons

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** [ViewModel] for [TopicLessonsFragment]. */
@FragmentScope
class TopicLessonViewModel @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val topicController: TopicController,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) {
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var topicStoryList: List<StorySummary>
  val itemList: MutableList<TopicLessonsItemViewModel> = ArrayList()

  val topicLessonLiveData: LiveData<List<TopicLessonsItemViewModel>> by lazy {
    Transformations.map(topicLiveData, ::processTopic)
  }

  private val topicLiveData: LiveData<EphemeralTopic> by lazy { getTopicList() }

  private fun getTopicList(): LiveData<EphemeralTopic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private val topicResultLiveData: LiveData<AsyncResult<EphemeralTopic>> by lazy {
    topicController.getTopic(profileId, topicId).toLiveData()
  }

  private fun processTopicResult(ephemeralResult: AsyncResult<EphemeralTopic>): EphemeralTopic {
    return when (ephemeralResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("TopicLessonFragment", "Failed to retrieve topic", ephemeralResult.error)
        EphemeralTopic.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralTopic.getDefaultInstance()
      is AsyncResult.Success -> ephemeralResult.value
    }
  }

  private fun processTopic(ephemeralTopic: EphemeralTopic): List<TopicLessonsItemViewModel> {
    if (ephemeralTopic.storiesList.isNotEmpty()) {
      topicStoryList = ephemeralTopic.topic.storyList
      itemList.clear()
      itemList.add(TopicLessonsTitleViewModel())
      ephemeralTopic.storiesList.forEachIndexed { index, ephemeralStorySummary ->
        itemList.add(
          StorySummaryViewModel(
            ephemeralStorySummary,
            fragment as StorySummarySelector,
            fragment as ChapterSummarySelector,
            resourceHandler,
            translationController,
            index
          )
        )
      }
    }
    return itemList
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  fun setStoryId(storyId: String) {
    this.storyId = storyId
  }

  fun getIndexOfStory(storySummary: StorySummary) = topicStoryList.indexOf(storySummary)
}

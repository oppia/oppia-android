package org.oppia.android.app.topic.lessons

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** [ViewModel] for [TopicLessonsFragment]. */
@FragmentScope
class TopicLessonViewModel @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val topicController: TopicController
) {
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var topicStoryList: List<StorySummary>
  val itemList: MutableList<TopicLessonsItemViewModel> = ArrayList()

  val topicLessonLiveData: LiveData<List<TopicLessonsItemViewModel>> by lazy {
    Transformations.map(topicLiveData, ::processTopic)
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId
    ).toLiveData()
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      oppiaLogger.e(
        "TopicLessonFragment",
        "Failed to retrieve topic",
        topic.getErrorOrNull()!!
      )
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }

  private fun processTopic(topic: Topic): List<TopicLessonsItemViewModel> {
    if (topic.storyList.isNotEmpty()) {
      topicStoryList = topic.storyList
      itemList.clear()
      itemList.add(TopicLessonsTitleViewModel())
      for (storySummary in topic.storyList) {
        itemList.add(
          StorySummaryViewModel(
            storySummary,
            fragment as StorySummarySelector,
            fragment as ChapterSummarySelector
          )
        )
      }
    }
    return itemList
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  fun setStoryId(storyId: String) {
    this.storyId = storyId
  }

  fun getIndexOfStory(storySummary: StorySummary) = topicStoryList.indexOf(storySummary)
}

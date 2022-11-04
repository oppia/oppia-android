package org.oppia.android.app.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.domain.topic.TopicListController

/** The ObservableViewModel for [TopicFragment]. */
@FragmentScope
class TopicViewModel @Inject constructor(
  private val topicListController: TopicListController,
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) : ObservableViewModel() {
  private var internalProfileId: Int = -1
  private lateinit var topicId: String

  private val topicResultLiveData: LiveData<AsyncResult<EphemeralTopic>> by lazy {
    topicController.getTopic(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId
    ).toLiveData()
  }

  private val topicListResultLiveData: LiveData<AsyncResult<PromotedActivityList>> by lazy {
    topicListController.getPromotedActivityList(
      ProfileId.newBuilder().setInternalId(internalProfileId).build()
    ).toLiveData()
  }

  val numberOfChaptersCompletedLiveData: LiveData<Int> by lazy {
    Transformations.map(topicListResultLiveData, ::computeNumberOfChaptersCompleted)
  }

  private fun computeNumberOfChaptersCompleted(
    topicListResult: AsyncResult<PromotedActivityList>
  ): Int {
    return when (topicListResult) {
      is AsyncResult.Failure -> -1
      is AsyncResult.Pending -> -1
      is AsyncResult.Success -> {
        var numberOfChaptersCompleted = 0
        topicListResult.value.promotedStoryList.recentlyPlayedStoryList.forEach {
          numberOfChaptersCompleted += it.completedChapterCount
        }
        topicListResult.value.promotedStoryList.suggestedStoryList.forEach {
          numberOfChaptersCompleted += it.completedChapterCount
        }
        topicListResult.value.promotedStoryList.olderPlayedStoryList.forEach {
          numberOfChaptersCompleted += it.completedChapterCount
        }
        numberOfChaptersCompleted
      }
    }
  }

  private val topicLiveData: LiveData<EphemeralTopic> by lazy {
    Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  val topicToolbarTitleLiveData: LiveData<String> by lazy {
    Transformations.map(topicLiveData) { ephemeralTopic ->
      val topicTitle =
        translationController.extractString(
          ephemeralTopic.topic.title, ephemeralTopic.writtenTranslationContext
        )
      resourceHandler.getStringInLocaleWithWrapping(R.string.topic_name, topicTitle)
    }
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  private fun processTopicResult(ephemeralResult: AsyncResult<EphemeralTopic>): EphemeralTopic {
    return when (ephemeralResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("TopicFragment", "Failed to retrieve Topic: ", ephemeralResult.error)
        EphemeralTopic.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralTopic.getDefaultInstance()
      is AsyncResult.Success -> ephemeralResult.value
    }
  }
}

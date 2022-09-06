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

/** The ObservableViewModel for [TopicFragment]. */
@FragmentScope
class TopicViewModel @Inject constructor(
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

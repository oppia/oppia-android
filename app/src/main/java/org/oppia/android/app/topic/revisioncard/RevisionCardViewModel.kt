package org.oppia.android.app.topic.revisioncard

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.RevisionCard
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** [ObservableViewModel] for revision card, providing rich text and worked examples */
@FragmentScope
class RevisionCardViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val topicController: TopicController,
  private val logger: ConsoleLogger
) : ObservableViewModel() {
  private lateinit var topicId: String
  private var subtopicId: Int = 0

  private val returnToTopicClickListener: ReturnToTopicClickListener =
    activity as ReturnToTopicClickListener

  val revisionCardLiveData: LiveData<RevisionCard> by lazy {
    processRevisionCardLiveData()
  }

  fun clickReturnToTopic(@Suppress("UNUSED_PARAMETER") v: View) {
    returnToTopicClickListener.onReturnToTopicClicked()
  }

  /** Sets the value of topicId, subtopicId and binding before anything else. */
  fun setSubtopicIdAndBinding(
    topicId: String,
    subtopicId: Int
  ) {
    this.topicId = topicId
    this.subtopicId = subtopicId
  }

  private val revisionCardResultLiveData: LiveData<AsyncResult<RevisionCard>> by lazy {
    topicController.getRevisionCard(topicId, subtopicId)
  }

  private fun processRevisionCardLiveData(): LiveData<RevisionCard> {
    return Transformations.map(revisionCardResultLiveData, ::processRevisionCard)
  }

  private fun processRevisionCard(
    revisionCardResult: AsyncResult<RevisionCard>
  ): RevisionCard {
    if (revisionCardResult.isFailure()) {
      logger.e(
        "RevisionCardFragment",
        "Failed to retrieve Revision Card",
        revisionCardResult.getErrorOrNull()!!
      )
    }

    return revisionCardResult.getOrDefault(
      RevisionCard.getDefaultInstance()
    )
  }
}

package org.oppia.android.app.topic.revisioncard

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.RevisionCard
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.HtmlParser
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import javax.inject.Inject

// TODO(#1633): Fix ViewModel to not depend on View
/** [ObservableViewModel] for revision card, providing rich text and worked examples */
@FragmentScope
class RevisionCardViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val topicController: TopicController,
  private val logger: ConsoleLogger,
  @DefaultResourceBucketName val gcsResourceName: String,
  @TopicHtmlParserEntityType val gcsEntityType: String
) : ObservableViewModel() {
  private lateinit var topicId: String
  private var subtopicId: Int = 0

  private val returnToTopicClickListener: ReturnToTopicClickListener =
    activity as ReturnToTopicClickListener

  val explanationLiveData: LiveData<CharSequence> by lazy {
    processExplanationLiveData()
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

  fun getTopicId(): String = topicId

  private val revisionCardResultLiveData: LiveData<AsyncResult<RevisionCard>> by lazy {
    topicController.getRevisionCard(topicId, subtopicId)
  }

  private fun processExplanationLiveData(): LiveData<CharSequence> {
    return Transformations.map(revisionCardResultLiveData, ::processExplanationResult)
  }

  private fun processExplanationResult(
    revisionCardResult: AsyncResult<RevisionCard>
  ): CharSequence {
    if (revisionCardResult.isFailure()) {
      logger.e(
        "RevisionCardFragment",
        "Failed to retrieve Revision Card",
        revisionCardResult.getErrorOrNull()!!
      )
    }
    val revisionCard = revisionCardResult.getOrDefault(
      RevisionCard.getDefaultInstance()
    )
    return revisionCard.pageContents.html
  }
}

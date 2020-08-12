package org.oppia.app.topic.revisioncard

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.RevisionCard
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.HtmlParser
import org.oppia.util.parser.TopicHtmlParserEntityType
import javax.inject.Inject

//TODO(#1633): Fix ViewModel to not depend on View
/** [ViewModel] for revision card, providing rich text and worked examples */
@FragmentScope
class RevisionCardViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val topicController: TopicController,
  private val logger: ConsoleLogger,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @TopicHtmlParserEntityType private val entityType: String
) : ViewModel() {
  private lateinit var topicId: String
  private var subtopicId: Int = 0
  private lateinit var view: TextView

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
    subtopicId: Int,
    view: TextView
  ) {
    this.topicId = topicId
    this.subtopicId = subtopicId
    this.view = view
  }

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
    return htmlParserFactory.create(

      resourceBucketName, entityType, topicId, /* imageCenterAlign= */ true
    ).parseOppiaHtml(revisionCard.pageContents.html, view)
  }
}

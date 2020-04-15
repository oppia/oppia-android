package org.oppia.app.topic.revisioncard

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.databinding.RevisionCardFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.RevisionCard
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.parser.HtmlParser
import org.oppia.util.parser.RevisionCardHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] for revision card, providing rich text and worked examples */
@FragmentScope
class RevisionCardViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory,
  val fragment: Fragment,
  @RevisionCardHtmlParserEntityType private val entityType: String
) : ViewModel() {
  private lateinit var topicId: String
  private lateinit var subtopicId: String
  private lateinit var binding: RevisionCardFragmentBinding
  private val returnToTopicClickListener: ReturnToTopicClickListener =
    fragment as ReturnToTopicClickListener

  var subtopicTitle: String = ""

  val explanationLiveData: LiveData<CharSequence> by lazy {
    processExplanationLiveData()
  }

  fun clickReturnToTopic(@Suppress("UNUSED_PARAMETER") v: View) {
    returnToTopicClickListener.onReturnToTopicClicked()
  }

  /** Sets the value of subtopicId and binding. Must be called before setting ViewModel to binding. */
  fun setSubtopicIdAndBinding(topicId: String, id: String, binding: RevisionCardFragmentBinding) {
    subtopicId = id
    this.topicId = topicId
    this.binding = binding
  }

  private val revisionCardResultLiveData: LiveData<AsyncResult<RevisionCard>> by lazy {
    topicController.getRevisionCard(topicId,subtopicId)
  }

  private fun processExplanationLiveData(): LiveData<CharSequence> {
    return Transformations.map(revisionCardResultLiveData, ::processExplanationResult)
  }

  private fun processExplanationResult(revisionCardResult: AsyncResult<RevisionCard>): CharSequence {
    if (revisionCardResult.isFailure()) {
      logger.e("RevisionCardFragment", "Failed to retrieve Revision Card", revisionCardResult.getErrorOrNull()!!)
    }
    val revisionCard = revisionCardResult.getOrDefault(RevisionCard.getDefaultInstance())
    subtopicTitle = revisionCard.subtopicTitle
    return htmlParserFactory.create(entityType, subtopicId, /* imageCenterAlign= */ true)
      .parseOppiaHtml(revisionCard.pageContents.html, binding.revisionCardExplanationText)
  }
}

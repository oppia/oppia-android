package org.oppia.app.topic.reviewcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.databinding.ReviewCardFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ReviewCard
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.parser.HtmlParser
import org.oppia.util.parser.ReviewCardHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ReviewCardViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory,
  @ReviewCardHtmlParserEntityType private val entityType: String
) : ViewModel() {
  private lateinit var topicName: String
  private lateinit var subtopicId: String
  private lateinit var binding: ReviewCardFragmentBinding

  val reviewCardLiveData: LiveData<ReviewCard> by lazy {
    processReviewCardLiveData()
  }

  val explanationLiveData: LiveData<CharSequence> by lazy {
    processExplanationLiveData()
  }

  /** Sets the value of subtopicId and binding. Must be called before setting ViewModel to binding */
  fun setSkillIdAndBinding(topicName: String, id: String, binding: ReviewCardFragmentBinding) {
    subtopicId = id
    this.topicName = topicName
    this.binding = binding
  }

  private val reviewCardResultLiveData: LiveData<AsyncResult<ReviewCard>> by lazy {
    topicController.getReviewCard(topicName,subtopicId)
  }

  private fun processReviewCardLiveData(): LiveData<ReviewCard> {
    return Transformations.map(reviewCardResultLiveData, ::processReviewCardResult)
  }

  private fun processExplanationLiveData(): LiveData<CharSequence> {
    return Transformations.map(reviewCardResultLiveData, ::processExplanationResult)
  }

  private fun processReviewCardResult(reviewCardResult: AsyncResult<ReviewCard>): ReviewCard {
    if (reviewCardResult.isFailure()) {
      logger.e("ReviewCardFragment", "Failed to retrieve Review Card", reviewCardResult.getErrorOrNull()!!)
    }
    return reviewCardResult.getOrDefault(ReviewCard.getDefaultInstance())
  }

  private fun processExplanationResult(reviewCardResult: AsyncResult<ReviewCard>): CharSequence {
    if (reviewCardResult.isFailure()) {
      logger.e("ReviewCardFragment", "Failed to retrieve Review Card", reviewCardResult.getErrorOrNull()!!)
    }
    val reviewCard = reviewCardResult.getOrDefault(ReviewCard.getDefaultInstance())
    return htmlParserFactory.create(entityType, subtopicId, /* imageCenterAlign= */ true)
      .parseOppiaHtml(reviewCard.explanation.html, binding.reviewCardExplanationText)
  }
}

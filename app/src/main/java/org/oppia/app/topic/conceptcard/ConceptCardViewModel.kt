package org.oppia.app.topic.conceptcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.databinding.ConceptCardFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ConceptCard
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.Logger
import org.oppia.util.parser.ConceptCardHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** [ViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ConceptCardViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory,
  @ConceptCardHtmlParserEntityType private val entityType: String,
  @DefaultResourceBucketName private val resourceBucketName: String
) : ViewModel() {
  private lateinit var skillId: String
  private lateinit var binding: ConceptCardFragmentBinding

  val conceptCardLiveData: LiveData<ConceptCard> by lazy {
    processConceptCardLiveData()
  }

  val explanationLiveData: LiveData<CharSequence> by lazy {
    processExplanationLiveData()
  }

  /** Sets the value of skillId and binding. Must be called before setting ViewModel to binding */
  fun setSkillIdAndBinding(id: String, binding: ConceptCardFragmentBinding) {
    skillId = id
    this.binding = binding
  }

  private val conceptCardResultLiveData: LiveData<AsyncResult<ConceptCard>> by lazy {
    topicController.getConceptCard(skillId)
  }

  private fun processConceptCardLiveData(): LiveData<ConceptCard> {
    return Transformations.map(conceptCardResultLiveData, ::processConceptCardResult)
  }

  private fun processExplanationLiveData(): LiveData<CharSequence> {
    return Transformations.map(conceptCardResultLiveData, ::processExplanationResult)
  }

  private fun processConceptCardResult(conceptCardResult: AsyncResult<ConceptCard>): ConceptCard {
    if (conceptCardResult.isFailure()) {
      logger.e("ConceptCardFragment", "Failed to retrieve Concept Card", conceptCardResult.getErrorOrNull()!!)
    }
    return conceptCardResult.getOrDefault(ConceptCard.getDefaultInstance())
  }

  private fun processExplanationResult(conceptCardResult: AsyncResult<ConceptCard>): CharSequence {
    if (conceptCardResult.isFailure()) {
      logger.e("ConceptCardFragment", "Failed to retrieve Concept Card", conceptCardResult.getErrorOrNull()!!)
    }
    val conceptCard = conceptCardResult.getOrDefault(ConceptCard.getDefaultInstance())
    return htmlParserFactory.create(resourceBucketName, entityType, skillId, /* imageCenterAlign= */ true)
      .parseOppiaHtml(conceptCard.explanation.html, binding.conceptCardExplanationText)
  }
}

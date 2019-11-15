package org.oppia.app.topic.conceptcard

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.databinding.ConceptCardFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ConceptCard
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** [ViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ConceptCardViewModel @Inject constructor(
  @ExplorationHtmlParserEntityType private val entityType: String,
  private val fragment: Fragment,
  private val topicController: TopicController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory
) : ViewModel() {

  private lateinit var skillId: String

  val conceptCard = ObservableField<ConceptCard>(ConceptCard.getDefaultInstance())

  private lateinit var binding: ConceptCardFragmentBinding

  /** Sets the value of skillId. Must be called before setting ViewModel to binding. */
  fun setSkillIdAndBinding(id: String, binding: ConceptCardFragmentBinding) {
    skillId = id
    this.binding = binding
    subscribeToConceptCard(topicController.getConceptCard(skillId))
  }

  private fun subscribeToConceptCard(conceptCardAsyncLiveData: LiveData<AsyncResult<ConceptCard>>) {
    val conceptCardLiveData = getConceptCard(conceptCardAsyncLiveData)
    conceptCardLiveData.observe(fragment, Observer<ConceptCard> {
      conceptCard.set(it)
      if (it.hasExplanation()) {
        val parsedHtmlString =
          htmlParserFactory.create(entityType, skillId).parseOppiaHtml(it.explanation.html, binding.explanationTextView)
        if (parsedHtmlString.isNotEmpty()) {
          binding.parsedExplanation = parsedHtmlString
        }
      }
    })
  }

  /** Helper for subscribeToConceptCard. */
  private fun getConceptCard(conceptCard: LiveData<AsyncResult<ConceptCard>>): LiveData<ConceptCard> {
    return Transformations.map(conceptCard, ::processConceptCard)
  }

  /** Helper for subscribeToConceptCard. */
  private fun processConceptCard(conceptCard: AsyncResult<ConceptCard>): ConceptCard {
    if (conceptCard.isFailure()) {
      logger.e("ConceptCardFragment", "Failed to retrieve concept card", conceptCard.getErrorOrNull()!!)
    }
    return conceptCard.getOrDefault(ConceptCard.getDefaultInstance())
  }
}

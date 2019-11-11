package org.oppia.app.topic.conceptcard

import android.text.Spannable
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ConceptCard
import org.oppia.app.model.SubtitledHtml
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** [ViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ConceptCardViewModel @Inject constructor(
  fragment: Fragment,
  private val topicController: TopicController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory
) : ViewModel() {

 private val conceptCardFragment = fragment as ConceptCardFragment

  /** Live Data for concept card explanation */
  val conceptCardLiveData: LiveData<ConceptCard> by lazy {
    processConceptCardLiveData()
  }

  /** LiveData for concept card explanation */
  val explanationLiveData: LiveData<Spannable> by lazy {
    processExplanationLiveData()
  }

  /** Live Data for concept card worked examples. */
  val workedExamplesLiveData: LiveData<List<SubtitledHtml>> by lazy {
    processWorkedExamplesLiveData()
  }

  private val conceptCardResultLiveData: LiveData<AsyncResult<ConceptCard>> by lazy {
    topicController.getConceptCard(conceptCardFragment.getSkillId())
  }

  private fun processConceptCardLiveData(): LiveData<ConceptCard> {
    return Transformations.map(conceptCardResultLiveData, ::processConceptCardResult)
  }

  private fun processExplanationLiveData(): LiveData<Spannable> {
    return Transformations.map(conceptCardResultLiveData, ::processConceptCardExplanation)
  }

  private fun processWorkedExamplesLiveData(): LiveData<List<SubtitledHtml>> {
    return Transformations.map(conceptCardResultLiveData, ::processConceptCardWorkExamples)
  }

  private fun processConceptCardResult(conceptCardResult: AsyncResult<ConceptCard>): ConceptCard {
    if (conceptCardResult.isFailure()) {
      logger.e("ConceptCardFragment", "Failed to retrieve Concept Card: " + conceptCardResult.getErrorOrNull())
    }
    return conceptCardResult.getOrDefault(ConceptCard.getDefaultInstance())
  }

  private fun processConceptCardExplanation(conceptCardResult: AsyncResult<ConceptCard>): Spannable {
    if (conceptCardResult.isFailure()) {
      logger.e("ConceptCardFragment", "Failed to retrieve Concept Card: " + conceptCardResult.getErrorOrNull())
    }
    val htmlParser = htmlParserFactory.create("skill", conceptCardFragment.getSkillId())
    val conceptCard = conceptCardResult.getOrDefault(ConceptCard.getDefaultInstance())
    return htmlParser.parseOppiaHtml(conceptCard.explanation.html, conceptCardFragment.getExplanationTextView())
  }

  private fun processConceptCardWorkExamples(conceptCardResult: AsyncResult<ConceptCard>): List<SubtitledHtml> {
    if (conceptCardResult.isFailure()) {
      logger.e("ConceptCardFragment", "Failed to retrieve Concept Card: " + conceptCardResult.getErrorOrNull())
    }
    return conceptCardResult.getOrDefault(ConceptCard.getDefaultInstance()).workedExampleList
  }
}

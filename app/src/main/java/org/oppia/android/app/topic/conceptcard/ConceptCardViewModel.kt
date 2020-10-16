package org.oppia.android.app.topic.conceptcard

import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ConceptCard
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.ConceptCardHtmlParserEntityType
import org.oppia.android.util.parser.HtmlParser
import javax.inject.Inject

// TODO(#1633): Fix ViewModel to not depend on View
/** [ObservableViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ConceptCardViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: ConsoleLogger,
  @DefaultResourceBucketName val gcsResourceName: String,
  @ConceptCardHtmlParserEntityType val gcsEntityType: String
) : ObservableViewModel() {
  private lateinit var skillId: String

  val conceptCardLiveData: LiveData<ConceptCard> by lazy {
    processConceptCardLiveData()
  }

  val explanationLiveData: LiveData<CharSequence> by lazy {
    processExplanationLiveData()
  }

  /** Sets the value of skillId and binding. Must be called before setting ViewModel to binding */
  fun setSkillIdAndBinding(id: String) {
    skillId = id
  }

  fun getSkillId(): String = skillId

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
      logger.e(
        "ConceptCardFragment",
        "Failed to retrieve Concept Card",
        conceptCardResult.getErrorOrNull()!!
      )
    }
    return conceptCardResult.getOrDefault(ConceptCard.getDefaultInstance())
  }

  private fun processExplanationResult(conceptCardResult: AsyncResult<ConceptCard>): CharSequence {
    if (conceptCardResult.isFailure()) {
      logger.e(
        "ConceptCardFragment",
        "Failed to retrieve Concept Card",
        conceptCardResult.getErrorOrNull()!!
      )
    }
    val conceptCard = conceptCardResult.getOrDefault(ConceptCard.getDefaultInstance())
    return conceptCard.explanation.html
  }
}

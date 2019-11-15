package org.oppia.app.topic.conceptcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ConceptCard
import org.oppia.app.model.ConceptCardModel
import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** [ViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ConceptCardViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: Logger
) : ViewModel() {
  private lateinit var skillId: String

  /** Live Data for concept card recyclerview */
  val conceptCardLiveData: LiveData<List<ConceptCardModel>> by lazy {
    processConceptCardLiveData()
  }

  /** Sets skillId used to get ConceptCard data. */
  fun setSkillId(id: String) {
    skillId = id
  }

  private val conceptCardResultLiveData: LiveData<AsyncResult<ConceptCard>> by lazy {
    topicController.getConceptCard(skillId)
  }

  private fun processConceptCardLiveData(): LiveData<List<ConceptCardModel>> {
    return Transformations.map(conceptCardResultLiveData, ::processConceptCardResult)
  }

  private fun processConceptCardResult(conceptCardResult: AsyncResult<ConceptCard>): List<ConceptCardModel> {
    if (conceptCardResult.isFailure()) {
      logger.e("ConceptCardFragment", "Failed to retrieve Concept Card", conceptCardResult.getErrorOrNull()!!)
    }
    val conceptCard = conceptCardResult.getOrThrow()
    val list = mutableListOf<ConceptCardModel>()
    list.add(ConceptCardModel.newBuilder().setSkillDescription(conceptCard.skillDescription).build())
    if (conceptCard.hasExplanation()) {
      list.add(ConceptCardModel.newBuilder().setExplanation(conceptCard.explanation).build())
    }
    conceptCard.workedExampleList.forEach {
      list.add(ConceptCardModel.newBuilder().setWorkedExample(it).build())
    }
    return list
  }
}

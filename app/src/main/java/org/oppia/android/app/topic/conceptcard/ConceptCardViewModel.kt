package org.oppia.android.app.topic.conceptcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ConceptCard
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject

/** [ObservableViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ConceptCardViewModel @Inject constructor(
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger
) : ObservableViewModel() {
  private lateinit var skillId: String

  val conceptCardLiveData: LiveData<ConceptCard> by lazy {
    processConceptCardLiveData()
  }

  fun setSkillId(id: String) {
    skillId = id
  }

  private val conceptCardResultLiveData: LiveData<AsyncResult<ConceptCard>> by lazy {
    topicController.getConceptCard(skillId)
  }

  private fun processConceptCardLiveData(): LiveData<ConceptCard> {
    return Transformations.map(conceptCardResultLiveData, ::processConceptCardResult)
  }

  private fun processConceptCardResult(conceptCardResult: AsyncResult<ConceptCard>): ConceptCard {
    if (conceptCardResult.isFailure()) {
      oppiaLogger.e(
        "ConceptCardFragment",
        "Failed to retrieve Concept Card",
        conceptCardResult.getErrorOrNull()!!
      )
    }
    return conceptCardResult.getOrDefault(ConceptCard.getDefaultInstance())
  }
}

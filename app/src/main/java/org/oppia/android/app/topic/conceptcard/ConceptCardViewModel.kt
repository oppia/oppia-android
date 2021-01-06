package org.oppia.android.app.topic.conceptcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ConceptCard
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** [ObservableViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ConceptCardViewModel @Inject constructor(
  private val topicController: TopicController,
  private val logger: ConsoleLogger
) : ObservableViewModel() {
  private lateinit var skillId: String

  private val itemViewModelList: MutableList<ConceptCardItemViewModel> = ArrayList()

  private val conceptCardLiveData: LiveData<ConceptCard> by lazy {
    processConceptCardLiveData()
  }

  val conceptCardItemsLiveData: LiveData<List<ConceptCardItemViewModel>> by lazy {
    Transformations.map(conceptCardLiveData, ::processConceptCardItemList)
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
      logger.e(
        "ConceptCardFragment",
        "Failed to retrieve Concept Card",
        conceptCardResult.getErrorOrNull()!!
      )
    }
    return conceptCardResult.getOrDefault(ConceptCard.getDefaultInstance())
  }

  private fun processConceptCardItemList(conceptCard: ConceptCard): List<ConceptCardItemViewModel> {
    itemViewModelList.clear()

    itemViewModelList.add(ConceptCardHeadingItemViewModel(conceptCard.skillDescription))
    conceptCard.workedExampleList.forEach { workedExample ->
      itemViewModelList.add(
        ConceptCardWorkedExampleItemViewModel(
          workedExample.contentId,
          workedExample.html
        )
      )
    }
    itemViewModelList.add(ConceptCardExplanationItemViewModel(conceptCard.explanation.html))

    return itemViewModelList
  }
}

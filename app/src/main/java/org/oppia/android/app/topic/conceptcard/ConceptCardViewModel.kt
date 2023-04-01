package org.oppia.android.app.topic.conceptcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralConceptCard
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** [ObservableViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ConceptCardViewModel @Inject constructor(
  private val topicController: TopicController,
  private val oppiaLogger: OppiaLogger
) : ObservableViewModel() {
  private lateinit var skillId: String
  private lateinit var profileId: ProfileId

  val conceptCardLiveData: LiveData<EphemeralConceptCard> by lazy {
    processConceptCardLiveData()
  }

  fun initialize(skillId: String, profileId: ProfileId) {
    this.skillId = skillId
    this.profileId = profileId
  }

  private val conceptCardResultLiveData: LiveData<AsyncResult<EphemeralConceptCard>> by lazy {
    topicController.getConceptCard(profileId, skillId).toLiveData()
  }

  private fun processConceptCardLiveData(): LiveData<EphemeralConceptCard> {
    return Transformations.map(conceptCardResultLiveData, ::processConceptCardResult)
  }

  private fun processConceptCardResult(
    conceptCardResult: AsyncResult<EphemeralConceptCard>
  ): EphemeralConceptCard {
    return when (conceptCardResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ConceptCardFragment", "Failed to retrieve Concept Card", conceptCardResult.error
        )
        EphemeralConceptCard.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralConceptCard.getDefaultInstance()
      is AsyncResult.Success -> conceptCardResult.value
    }
  }
}

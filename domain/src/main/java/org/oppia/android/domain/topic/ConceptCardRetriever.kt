package org.oppia.android.domain.topic

import org.oppia.android.app.model.ConceptCard
import org.oppia.android.app.model.ConceptCardList
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject

/** Retriever for [ConceptCard] objects from the local filesystem. */
class ConceptCardRetriever @Inject constructor(
  private val assetRepository: AssetRepository,
) {
  /**
   * Returns a [ConceptCard] corresponding to the specified skill ID, loaded from the filesystem.
   */
  fun loadConceptCard(skillId: String): ConceptCard {
    val conceptCardList =
      assetRepository.loadProtoFromLocalAssets(
        assetName = "skills",
        baseMessage = ConceptCardList.getDefaultInstance()
      )
    val conceptCard = conceptCardList.conceptCardsList.find { it.skillId == skillId }
    return conceptCard ?: error("Failed to load concept card for skill: $skillId")
  }
}

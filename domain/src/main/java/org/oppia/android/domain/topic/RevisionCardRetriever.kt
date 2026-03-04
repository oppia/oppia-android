package org.oppia.android.domain.topic

import org.oppia.android.app.model.RevisionCard
import org.oppia.android.app.model.SubtopicRecord
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject

/** Retriever for [RevisionCard] objects from the local filesystem. */
class RevisionCardRetriever @Inject constructor(
  private val assetRepository: AssetRepository,
) {
  /**
   * Returns a [RevisionCard] given a subtopic ID in the specific topic, loaded from the filesystem.
   */
  fun loadRevisionCard(topicId: String, subtopicId: Int): RevisionCard {
    val subtopicRecord = assetRepository.loadProtoFromLocalAssets(
      assetName = "${topicId}_$subtopicId",
      baseMessage = SubtopicRecord.getDefaultInstance()
    )
    return RevisionCard.newBuilder().apply {
      subtopicTitle = subtopicRecord.title
      pageContents = subtopicRecord.pageContents
      putAllRecordedVoiceovers(subtopicRecord.recordedVoiceoverMap)
      putAllWrittenTranslations(subtopicRecord.writtenTranslationMap)
    }.build()
  }
}

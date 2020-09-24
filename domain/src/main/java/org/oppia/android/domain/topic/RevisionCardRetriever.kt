package org.oppia.android.domain.topic

import org.oppia.android.app.model.RevisionCard
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.domain.util.JsonAssetRetriever
import javax.inject.Inject

// TODO(#1580): Restrict access using Bazel visibilities.
/** Retriever for [RevisionCard] objects from the filesystem. */
class RevisionCardRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever
) {
  /**
   * Returns a [RevisionCard] given a subtopic ID in the specific topic, loaded from the filesystem.
   */
  fun loadRevisionCard(topicId: String, subtopicId: Int): RevisionCard {
    val subtopicJsonObject =
      jsonAssetRetriever.loadJsonFromAsset(topicId + "_" + subtopicId + ".json")
        ?: return RevisionCard.getDefaultInstance()
    val subtopicData = subtopicJsonObject.getJSONObject("page_contents")!!
    val subtopicTitle = subtopicJsonObject.getString("subtopic_title")!!
    return RevisionCard.newBuilder()
      .setSubtopicTitle(subtopicTitle)
      .setPageContents(
        SubtitledHtml.newBuilder()
          .setHtml(subtopicData.getJSONObject("subtitled_html").getString("html"))
          .setContentId(
            subtopicData.getJSONObject("subtitled_html").getString(
              "content_id"
            )
          )
          .build()
      )
      .build()
  }
}

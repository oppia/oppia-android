package org.oppia.android.domain.topic

import org.json.JSONObject
import org.oppia.android.app.model.HtmlTranslationList
import org.oppia.android.app.model.RevisionCard
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.SubtopicRecord
import org.oppia.android.app.model.Translation
import org.oppia.android.app.model.TranslationMapping
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.domain.util.getStringFromArray
import org.oppia.android.domain.util.getStringFromObject
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import javax.inject.Inject

// TODO(#1580): Restrict access using Bazel visibilities.
/** Retriever for [RevisionCard] objects from the filesystem. */
class RevisionCardRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val assetRepository: AssetRepository,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean
) {
  /**
   * Returns a [RevisionCard] given a subtopic ID in the specific topic, loaded from the filesystem.
   */
  fun loadRevisionCard(topicId: String, subtopicId: Int): RevisionCard {
    return if (loadLessonProtosFromAssets) {
      val subtopicRecord = assetRepository.loadProtoFromLocalAssets(
        assetName = "${topicId}_$subtopicId",
        baseMessage = SubtopicRecord.getDefaultInstance()
      )
      RevisionCard.newBuilder().apply {
        subtopicTitle = subtopicRecord.subtopicTitle
        pageContents = subtopicRecord.pageContents
        putAllRecordedVoiceover(subtopicRecord.recordedVoiceoverMap)
        putAllWrittenTranslation(subtopicRecord.writtenTranslationMap)
      }.build()
    } else loadRevisionCardFromJson(topicId, subtopicId)
  }

  private fun loadRevisionCardFromJson(topicId: String, subtopicId: Int): RevisionCard {
    val subtopicJsonObject =
      jsonAssetRetriever.loadJsonFromAsset(topicId + "_" + subtopicId + ".json")
        ?: return RevisionCard.getDefaultInstance()
    val subtopicData = subtopicJsonObject.getJSONObject("page_contents")
    val subtopicTitle = subtopicJsonObject.getStringFromObject("subtopic_title")
    return RevisionCard.newBuilder()
      .setSubtopicTitle(subtopicTitle)
      .setPageContents(
        SubtitledHtml.newBuilder()
          .setHtml(subtopicData.getJSONObject("subtitled_html").getStringFromObject("html"))
          .setContentId(
            subtopicData.getJSONObject("subtitled_html").getStringFromObject(
              "content_id"
            )
          )
          .build()
      )
      .putAllWrittenTranslation(
        createWrittenTranslationMappingsFromJson(subtopicData.getJSONObject("written_translations"))
      )
      .build()
  }

  private fun createWrittenTranslationMappingsFromJson(
    writtenTranslations: JSONObject
  ): Map<String, TranslationMapping> {
    val translationsMappingJson = writtenTranslations.getJSONObject("translations_mapping")
    return translationsMappingJson.keys().asSequence().filter { contentId ->
      translationsMappingJson.getJSONObject(contentId).length() != 0
    }.associateWith { contentId ->
      val translationJson = translationsMappingJson.getJSONObject(contentId)
      TranslationMapping.newBuilder().apply {
        putAllTranslationMapping(
          translationJson.keys().asSequence().associateWith { languageCode ->
            createTranslationFromJson(translationJson.getJSONObject(languageCode))
          }
        )
      }.build()
    }
  }

  private fun createTranslationFromJson(translatorJson: JSONObject): Translation =
    Translation.newBuilder().apply {
      val translationJson = translatorJson.getJSONObject("translation")
      needsUpdate = translatorJson.getBoolean("needs_update")
      when (val dataFormat = translatorJson.getStringFromObject("data_format")) {
        "html", "unicode" -> html = translationJson.getStringFromObject("translation")
        "set_of_normalized_string", "set_of_unicode_string" -> {
          val array = translationJson.getJSONArray("translations")
          htmlList = HtmlTranslationList.newBuilder().apply {
            for (i in 0 until array.length()) {
              addHtml(array.getStringFromArray(i))
            }
          }.build()
        }
        else -> error("Unsupported data format: $dataFormat")
      }
    }.build()
}

package org.oppia.android.domain.topic

import org.json.JSONArray
import org.json.JSONObject
import org.oppia.android.app.model.HtmlTranslationList
import org.oppia.android.app.model.StudyGuide
import org.oppia.android.app.model.StudyGuideSection
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.SubtitledUnicode
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
/** Retriever for [StudyGuide] objects from the filesystem. */
class StudyGuideRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val assetRepository: AssetRepository,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean
) {
  /**
   * Returns a [StudyGuide] given a subtopic ID in the specific topic, loaded from the filesystem.
   *
   * Study guides are stored alongside revision cards in per-subtopic [SubtopicRecord] assets. A
   * record that predates study guide support has no sections, in which case the returned
   * [StudyGuide] has an empty sections list.
   */
  fun loadStudyGuide(topicId: String, subtopicId: Int): StudyGuide {
    return if (loadLessonProtosFromAssets) {
      val subtopicRecord = assetRepository.loadProtoFromLocalAssets(
        assetName = "${topicId}_$subtopicId",
        baseMessage = SubtopicRecord.getDefaultInstance()
      )
      StudyGuide.newBuilder().apply {
        subtopicTitle = subtopicRecord.title
        addAllSections(subtopicRecord.sectionsList)
        putAllWrittenTranslations(subtopicRecord.writtenTranslationMap)
      }.build()
    } else loadStudyGuideFromJson(topicId, subtopicId)
  }

  private fun loadStudyGuideFromJson(topicId: String, subtopicId: Int): StudyGuide {
    val subtopicJsonObject =
      jsonAssetRetriever.loadJsonFromAsset(topicId + "_" + subtopicId + ".json")
        ?: return StudyGuide.getDefaultInstance()
    val subtopicTitle = SubtitledHtml.newBuilder().apply {
      contentId = "title"
      html = subtopicJsonObject.getStringFromObject("subtopic_title")
    }.build()
    return StudyGuide.newBuilder().apply {
      this.subtopicTitle = subtopicTitle
      addAllSections(createSectionsFromJson(subtopicJsonObject.optJSONArray("sections")))
      // Local JSON assets retain the legacy page_contents block for the revision card flow, so
      // study guide translations are sourced from its content ID-keyed translations_mapping. The
      // newer web study guide structure tracks translations separately, but that only affects the
      // proto pipeline (which delivers them via SubtopicRecord's written_translation map).
      putAllWrittenTranslations(
        createWrittenTranslationMappingsFromJson(
          subtopicJsonObject.optJSONObject("page_contents")
            ?.optJSONObject("written_translations")
        )
      )
    }.build()
  }

  private fun createSectionsFromJson(sectionsJson: JSONArray?): List<StudyGuideSection> {
    if (sectionsJson == null) return listOf()
    return (0 until sectionsJson.length()).map { index ->
      val sectionJson = sectionsJson.getJSONObject(index)
      val headingJson = sectionJson.getJSONObject("heading")
      val contentJson = sectionJson.getJSONObject("content")
      StudyGuideSection.newBuilder().apply {
        heading = SubtitledUnicode.newBuilder().apply {
          contentId = headingJson.getStringFromObject("content_id")
          unicodeStr = headingJson.getStringFromObject("unicode_str")
        }.build()
        content = SubtitledHtml.newBuilder().apply {
          contentId = contentJson.getStringFromObject("content_id")
          html = contentJson.getStringFromObject("html")
        }.build()
      }.build()
    }
  }

  private fun createWrittenTranslationMappingsFromJson(
    writtenTranslations: JSONObject?
  ): Map<String, TranslationMapping> {
    val translationsMappingJson = writtenTranslations?.optJSONObject("translations_mapping")
    return translationsMappingJson?.keys()?.asSequence()?.filter { contentId ->
      translationsMappingJson.getJSONObject(contentId).length() != 0
    }?.associateWith { contentId ->
      val translationJson = translationsMappingJson.getJSONObject(contentId)
      TranslationMapping.newBuilder().apply {
        putAllTranslationMapping(
          translationJson.keys().asSequence().associateWith { languageCode ->
            createTranslationFromJson(translationJson.getJSONObject(languageCode))
          }
        )
      }.build()
    } ?: mapOf()
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

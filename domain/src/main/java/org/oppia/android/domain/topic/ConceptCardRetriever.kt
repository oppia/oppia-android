package org.oppia.android.domain.topic

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.oppia.android.app.model.ConceptCard
import org.oppia.android.app.model.ConceptCardList
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.Translation
import org.oppia.android.app.model.TranslationMapping
import org.oppia.android.app.model.Voiceover
import org.oppia.android.app.model.VoiceoverMapping
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import javax.inject.Inject
import org.oppia.android.domain.util.getStringFromObject

// TODO(#1580): Restrict access using Bazel visibilities.
/** Retriever for [ConceptCard] objects from the filesystem. */
class ConceptCardRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val assetRepository: AssetRepository,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean
) {
  /**
   * Returns a [ConceptCard] corresponding to the specified skill ID, loaded from the filesystem.
   */
  fun loadConceptCard(skillId: String): ConceptCard {
    val conceptCard = if (loadLessonProtosFromAssets) {
      val conceptCardList =
        assetRepository.loadProtoFromLocalAssets(
          assetName = "skills",
          baseMessage = ConceptCardList.getDefaultInstance()
        )
      conceptCardList.conceptCardsList.find { it.skillId == skillId }
    } else loadConceptCardFromJson(skillId)
    return conceptCard ?: error("Failed to load concept card for skill: $skillId")
  }

  private fun loadConceptCardFromJson(skillId: String): ConceptCard? {
    try {
      val skillData = getSkillJsonObject(skillId)
      if (skillData.length() <= 0) {
        return ConceptCard.getDefaultInstance()
      }
      val skillContents = skillData.getJSONObject("skill_contents")
      val workedExamplesList = createWorkedExamplesFromJson(
        skillContents.getJSONArray(
          "worked_examples"
        )
      )

      val recordedVoiceoverMapping = hashMapOf<String, VoiceoverMapping>()
      recordedVoiceoverMapping["explanation"] = createRecordedVoiceoversFromJson(
        skillContents
          .optJSONObject("recorded_voiceovers")
          .optJSONObject("voiceovers_mapping")
          .optJSONObject(
            skillContents.optJSONObject("explanation").optString("content_id")
          )!!
      )
      for (workedExample in workedExamplesList) {
        recordedVoiceoverMapping[workedExample.contentId] = createRecordedVoiceoversFromJson(
          skillContents
            .optJSONObject("recorded_voiceovers")
            .optJSONObject("voiceovers_mapping")
            .optJSONObject(workedExample.contentId)
        )
      }

      val writtenTranslationMapping = hashMapOf<String, TranslationMapping>()
      writtenTranslationMapping["explanation"] = createWrittenTranslationFromJson(
        skillContents
          .optJSONObject("written_translations")
          .optJSONObject("translations_mapping")
          .optJSONObject(
            skillContents.optJSONObject("explanation").optString("content_id")
          )!!
      )
      for (workedExample in workedExamplesList) {
        writtenTranslationMapping[workedExample.contentId] = createWrittenTranslationFromJson(
          skillContents
            .optJSONObject("written_translations")
            .optJSONObject("translations_mapping")
            .optJSONObject(workedExample.contentId)
        )
      }

      return ConceptCard.newBuilder()
        .setSkillId(skillData.getStringFromObject("id"))
        .setSkillDescription(skillData.getStringFromObject("description"))
        .setExplanation(
          SubtitledHtml.newBuilder()
            .setHtml(skillContents.getJSONObject("explanation").getStringFromObject("html"))
            .setContentId(
              skillContents.getJSONObject("explanation").getStringFromObject(
                "content_id"
              )
            ).build()
        )
        .addAllWorkedExample(workedExamplesList)
        .putAllWrittenTranslation(writtenTranslationMapping)
        .putAllRecordedVoiceover(recordedVoiceoverMapping)
        .build()
    } catch (e: JSONException) {
      return null
    }
  }

  private fun getSkillJsonObject(skillId: String): JSONObject {
    val skillJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("skills.json")?.optJSONArray("skills")
      ?: return JSONObject("")
    for (i in 0 until skillJsonArray.length()) {
      val currentSkillJsonObject = skillJsonArray.optJSONObject(i)
      if (skillId == currentSkillJsonObject.optString("id")) {
        return currentSkillJsonObject
      }
    }
    return JSONObject("")
  }

  private fun createWorkedExamplesFromJson(workedExampleData: JSONArray): List<SubtitledHtml> {
    val workedExampleList = mutableListOf<SubtitledHtml>()
    for (i in 0 until workedExampleData.length()) {
      val workedExampleJson = workedExampleData.getJSONObject(i)
      // The question part of worked examples is not currently supported by the app.
      workedExampleList += createSubtitledHtml(workedExampleJson.getJSONObject("explanation"))
    }
    return workedExampleList
  }

  private fun createSubtitledHtml(
    subtitledHtmlJson: JSONObject
  ): SubtitledHtml = SubtitledHtml.newBuilder().apply {
    contentId = subtitledHtmlJson.getStringFromObject("content_id")
    html = subtitledHtmlJson.getStringFromObject("html")
  }.build()

  private fun createWrittenTranslationFromJson(
    translationMappingJsonObject: JSONObject?
  ): TranslationMapping {
    if (translationMappingJsonObject == null) {
      return TranslationMapping.getDefaultInstance()
    }
    val translationMappingBuilder = TranslationMapping.newBuilder()
    val languages = translationMappingJsonObject.keys()
    while (languages.hasNext()) {
      val language = languages.next()
      val translationJson = translationMappingJsonObject.optJSONObject(language)
      val translation = Translation.newBuilder()
        .setHtml(translationJson.optString("translation"))
        .setNeedsUpdate(translationJson.optBoolean("needs_update"))
        .build()
      translationMappingBuilder.putTranslationMapping(language, translation)
    }
    return translationMappingBuilder.build()
  }

  private fun createRecordedVoiceoversFromJson(
    voiceoverMappingJsonObject: JSONObject?
  ): VoiceoverMapping {
    if (voiceoverMappingJsonObject == null) {
      return VoiceoverMapping.getDefaultInstance()
    }
    val voiceoverMappingBuilder = VoiceoverMapping.newBuilder()
    val languages = voiceoverMappingJsonObject.keys()
    while (languages.hasNext()) {
      val language = languages.next()
      val voiceoverJson = voiceoverMappingJsonObject.optJSONObject(language)
      val voiceover = Voiceover.newBuilder()
        .setFileName(voiceoverJson.optString("filename"))
        .setNeedsUpdate(voiceoverJson.optBoolean("needs_update"))
        .setFileSizeBytes(voiceoverJson.optLong("file_size_bytes"))
        .build()
      voiceoverMappingBuilder.putVoiceoverMapping(language, voiceover)
    }
    return voiceoverMappingBuilder.build()
  }
}

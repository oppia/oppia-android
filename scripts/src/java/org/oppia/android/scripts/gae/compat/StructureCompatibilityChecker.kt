package org.oppia.android.scripts.gae.compat

import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.AudioVoiceoverHasInvalidAudioFormat
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.HtmlInTitleOrDescription
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.HtmlUnexpectedlyInUnicodeContent
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.MissingRequiredXlationLangForContentTranslation
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.MissingRequiredXlationLangForTitleOrDescFromWeb
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.StateHasInvalidInteractionId
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.StateSchemaVersionTooNew
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.TextHasInvalidTags
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.TextReferencesInvalidImageFormat
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.TextUsesImageTagWithMissingFilePath
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.ThumbnailHasInvalidColor
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.ThumbnailHasInvalidImageFormat
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.TopicHasNoKnownDependencies
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.TranslatedTextHasInvalidTags
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.UnsupportedDefaultLanguageCode
import org.oppia.android.scripts.gae.compat.SubtitledHtmlCollector.SubtitledText
import org.oppia.android.scripts.gae.json.GaeAnswerGroup
import org.oppia.android.scripts.gae.json.GaeCustomizationArgValue
import org.oppia.android.scripts.gae.json.GaeEntityTranslations
import org.oppia.android.scripts.gae.json.GaeExploration
import org.oppia.android.scripts.gae.json.GaeHint
import org.oppia.android.scripts.gae.json.GaeInteractionCustomizationArgsMap
import org.oppia.android.scripts.gae.json.GaeInteractionInstance
import org.oppia.android.scripts.gae.json.GaeOutcome
import org.oppia.android.scripts.gae.json.GaeRecordedVoiceovers
import org.oppia.android.scripts.gae.json.GaeSkill
import org.oppia.android.scripts.gae.json.GaeSkillContents
import org.oppia.android.scripts.gae.json.GaeSolution
import org.oppia.android.scripts.gae.json.GaeState
import org.oppia.android.scripts.gae.json.GaeStory
import org.oppia.android.scripts.gae.json.GaeStoryNode
import org.oppia.android.scripts.gae.json.GaeSubtitledHtml
import org.oppia.android.scripts.gae.json.GaeSubtitledUnicode
import org.oppia.android.scripts.gae.json.GaeSubtopic
import org.oppia.android.scripts.gae.json.GaeSubtopicPage
import org.oppia.android.scripts.gae.json.GaeSubtopicPageContents
import org.oppia.android.scripts.gae.json.GaeTopic
import org.oppia.android.scripts.gae.json.GaeTranslatedContent
import org.oppia.android.scripts.gae.json.GaeWorkedExample
import org.oppia.android.scripts.gae.json.GaeWrittenTranslation
import org.oppia.android.scripts.gae.json.GaeWrittenTranslations
import org.oppia.android.scripts.gae.json.VersionedStructure
import org.oppia.android.scripts.gae.proto.LocalizationTracker
import org.oppia.android.scripts.gae.proto.LocalizationTracker.Companion.parseColorRgb
import org.oppia.android.scripts.gae.proto.LocalizationTracker.Companion.resolveLanguageCode
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContainerId
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContentContext.DESCRIPTION
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContentContext.TITLE
import org.oppia.proto.v1.structure.LanguageType

// TODO: Check SVG compatibility?
// TODO: Check image validity?
// TODO: Check audio validity?
// TODO: Check HTML parsability?
// TODO: Check math exp parsability?

class StructureCompatibilityChecker(
  private val constraints: CompatibilityConstraints,
  private val localizationTracker: LocalizationTracker,
  private val subtitledHtmlCollector: SubtitledHtmlCollector
) {
  fun isTopicItselfCompatible(gaeTopic: GaeTopic): CompatibilityResult {
    val containerId = ContainerId.createFrom(gaeTopic)
    val defaultLanguage = gaeTopic.languageCode.resolveLanguageCode()
    return CompatibilityResult.createFrom {
      gaeTopic.id.checkIsValidTopicId(containerId) +
        gaeTopic.name.checkTitleOrDescTextForHtml(containerId) +
        gaeTopic.description.checkTitleOrDescTextForHtml(containerId) +
        gaeTopic.thumbnailFilename.checkThumbnailFilename(containerId) +
        gaeTopic.thumbnailBgColor.checkBackgroundHexColor(containerId) +
        gaeTopic.languageCode.checkDefaultLanguageCode(containerId) +
        checkHasRequiredWebTranslationsFor(containerId, defaultLanguage, TITLE, DESCRIPTION) +
        gaeTopic.subtopics.flatMap { checkSubtopicCompatibility(gaeTopic.id, it, defaultLanguage) }
    }
  }

  private fun checkSubtopicCompatibility(
    topicId: String,
    gaeSubtopic: GaeSubtopic,
    defaultLanguage: LanguageType
  ): List<CompatibilityFailure> {
    val containerId = ContainerId.createFrom(topicId, gaeSubtopic)
    return gaeSubtopic.title.checkTitleOrDescTextForHtml(containerId) +
      gaeSubtopic.thumbnailFilename.checkThumbnailFilename(containerId) +
      gaeSubtopic.thumbnailBgColor.checkBackgroundHexColor(containerId) +
      checkHasRequiredWebTranslationsFor(containerId, defaultLanguage, TITLE)
  }

  fun isStoryItselfCompatible(gaeStory: GaeStory): CompatibilityResult {
    val containerId = ContainerId.createFrom(gaeStory)
    val defaultLanguage = gaeStory.languageCode.resolveLanguageCode()
    return CompatibilityResult.createFrom {
      gaeStory.title.checkTitleOrDescTextForHtml(containerId) +
        gaeStory.description.checkTitleOrDescTextForHtml(containerId) +
        gaeStory.thumbnailFilename.checkThumbnailFilename(containerId) +
        gaeStory.thumbnailBgColor.checkBackgroundHexColor(containerId) +
        gaeStory.languageCode.checkDefaultLanguageCode(containerId) +
        checkHasRequiredWebTranslationsFor(containerId, defaultLanguage, TITLE, DESCRIPTION) +
        gaeStory.storyContents.nodes.flatMap {
          checkStoryNodeCompatibility(gaeStory, it, defaultLanguage, containerId)
        }
    }
  }

  private fun checkStoryNodeCompatibility(
    gaeStory: GaeStory,
    gaeStoryNode: GaeStoryNode,
    defaultLanguage: LanguageType,
    storyContainerId: ContainerId
  ): List<CompatibilityFailure> {
    return ContainerId.createFrom(gaeStory, gaeStoryNode)?.let { containerId ->
      return gaeStoryNode.title.checkTitleOrDescTextForHtml(containerId) +
        gaeStoryNode.description.checkTitleOrDescTextForHtml(containerId) +
        gaeStoryNode.thumbnailFilename.checkThumbnailFilename(containerId) +
        gaeStoryNode.thumbnailBgColor.checkBackgroundHexColor(containerId) +
        checkHasRequiredWebTranslationsFor(containerId, defaultLanguage, TITLE, DESCRIPTION)
    } ?: listOf(CompatibilityFailure.StoryIsMissingExplorationId(gaeStory.id, storyContainerId))
  }

  fun isSubtopicPageItselfCompatible(
    gaeSubtopicPage: GaeSubtopicPage,
    correspondingGaeSubtopic: GaeSubtopic
  ): CompatibilityResult {
    val containerId = ContainerId.createFrom(gaeSubtopicPage, correspondingGaeSubtopic)
    val expectedTranslatedContentIds =
      subtitledHtmlCollector.collectSubtitles(gaeSubtopicPage).collectContentIds()
    val defaultLanguage = gaeSubtopicPage.languageCode.resolveLanguageCode()
    return CompatibilityResult.createFrom {
      checkSubtopicPageContentsCompatibility(
        containerId, gaeSubtopicPage.pageContents, expectedTranslatedContentIds, defaultLanguage
      ) + gaeSubtopicPage.languageCode.checkDefaultLanguageCode(containerId)
    }
  }

  private fun checkSubtopicPageContentsCompatibility(
    origin: ContainerId,
    subtopicPageContents: GaeSubtopicPageContents,
    expectedTranslatedContentIds: Set<String>,
    defaultLanguage: LanguageType
  ): List<CompatibilityFailure> {
    return subtopicPageContents.subtitledHtml.checkHasValidHtml(origin) +
      checkWrittenTranslationsCompatibility(
        origin,
        subtopicPageContents.writtenTranslations,
        expectedTranslatedContentIds,
        defaultLanguage
      ) + checkRecordedVoiceoversCompatibility(origin, subtopicPageContents.recordedVoiceovers)
  }

  fun isExplorationItselfCompatible(completeExploration: CompleteExploration): CompatibilityResult {
    val containerId = ContainerId.createFrom(completeExploration.exploration)
    val expectedTranslatedContentIds =
      subtitledHtmlCollector.collectSubtitles(completeExploration).collectContentIds()
    val defaultLanguage = completeExploration.exploration.languageCode.resolveLanguageCode()
    return CompatibilityResult.createFrom {
      checkAllEntityTranslationsCompatibility(
        containerId, completeExploration.translations, expectedTranslatedContentIds, defaultLanguage
      ) + checkExplorationCompatibility(
        containerId, completeExploration.exploration, defaultLanguage
      )
    }
  }

  private fun checkExplorationCompatibility(
    origin: ContainerId,
    gaeExploration: GaeExploration,
    defaultLanguage: LanguageType
  ): List<CompatibilityFailure> {
    return gaeExploration.title.checkTitleOrDescTextForHtml(origin) +
      gaeExploration.languageCode.checkDefaultLanguageCode(origin) +
      checkHasRequiredWebTranslationsFor(origin, defaultLanguage, TITLE) +
      gaeExploration.statesSchemaVersion.checkIsValidStateSchemaVersion(origin) +
      gaeExploration.states.flatMap { (stateName, state) ->
        checkStateCompatibility(origin, stateName, state)
      }
  }

  private fun checkStateCompatibility(
    origin: ContainerId,
    stateName: String,
    gaeState: GaeState
  ): List<CompatibilityFailure> {
    return gaeState.content.checkHasValidHtml(origin) +
      checkInteractionInstanceCompatibility(origin, stateName, gaeState.interaction) +
      checkRecordedVoiceoversCompatibility(origin, gaeState.recordedVoiceovers)
  }

  private fun checkInteractionInstanceCompatibility(
    origin: ContainerId,
    stateName: String,
    gaeInteractionInstance: GaeInteractionInstance
  ): List<CompatibilityFailure> {
    return gaeInteractionInstance.id.checkIsValidInteractionId(stateName, origin) +
      checkInteractionCustArgsCompatibility(origin, gaeInteractionInstance.customizationArgs) +
      checkAnswerGroupsCompatibility(origin, gaeInteractionInstance.answerGroups) +
      checkOutcomeCompatibility(origin, gaeInteractionInstance.defaultOutcome) +
      checkHintsCompatibility(origin, gaeInteractionInstance.hints) +
      checkSolutionCompatibility(origin, gaeInteractionInstance.solution)
  }

  private fun checkInteractionCustArgsCompatibility(
    origin: ContainerId,
    gaeCustomizationArgs: GaeInteractionCustomizationArgsMap
  ): List<CompatibilityFailure> {
    return gaeCustomizationArgs.customizationArgs.values.flatMap { argValue ->
      when (argValue) {
        is GaeCustomizationArgValue.GaeImageWithRegions, is GaeCustomizationArgValue.SingleBoolean,
        is GaeCustomizationArgValue.SingleInteger, is GaeCustomizationArgValue.StringList ->
          emptyList()
        is GaeCustomizationArgValue.SubtitledUnicode -> argValue.value.checkHasNoValidHtml(origin)
        is GaeCustomizationArgValue.SubtitledTextList ->
          argValue.value.flatMap { it.checkHasValidHtml(origin) }
      }
    }
  }

  private fun checkAnswerGroupsCompatibility(
    origin: ContainerId,
    gaeAnswerGroups: List<GaeAnswerGroup>
  ) = gaeAnswerGroups.flatMap { checkOutcomeCompatibility(origin, it.outcome) }

  private fun checkOutcomeCompatibility(origin: ContainerId, gaeOutcome: GaeOutcome?) =
    gaeOutcome?.feedback?.checkHasValidHtml(origin) ?: emptyList()

  private fun checkHintsCompatibility(origin: ContainerId, gaeHints: List<GaeHint>) =
    gaeHints.flatMap { it.hintContent.checkHasValidHtml(origin) }

  private fun checkSolutionCompatibility(origin: ContainerId, gaeSolution: GaeSolution?) =
    gaeSolution?.explanation?.checkHasValidHtml(origin) ?: emptyList()

  fun isSkillItselfCompatible(gaeSkill: GaeSkill): CompatibilityResult {
    val containerId = ContainerId.createFrom(gaeSkill)
    val contentIdsToXlate = subtitledHtmlCollector.collectSubtitles(gaeSkill).collectContentIds()
    val defaultLanguage = gaeSkill.languageCode.resolveLanguageCode()
    return CompatibilityResult.createFrom {
      // Note that Oppia wbe translations don't include skill descriptions, so they aren't checked.
      gaeSkill.description.checkTitleOrDescTextForHtml(containerId) +
        gaeSkill.languageCode.checkDefaultLanguageCode(containerId) +
        checkSkillContentsCompatibility(
          containerId, gaeSkill.skillContents, contentIdsToXlate, defaultLanguage
        )
    }
  }

  private fun checkSkillContentsCompatibility(
    origin: ContainerId,
    gaeSkillContents: GaeSkillContents,
    expectedTranslatedContentIds: Set<String>,
    defaultLanguage: LanguageType
  ): List<CompatibilityFailure> {
    return gaeSkillContents.explanation.checkHasValidHtml(origin) +
      gaeSkillContents.workedExamples.flatMap { checkWorkedExampleCompatibility(origin, it) } +
      checkWrittenTranslationsCompatibility(
        origin, gaeSkillContents.writtenTranslations, expectedTranslatedContentIds, defaultLanguage
      ) + checkRecordedVoiceoversCompatibility(origin, gaeSkillContents.recordedVoiceovers)
  }

  private fun checkWorkedExampleCompatibility(
    origin: ContainerId,
    gaeWorkedExample: GaeWorkedExample
  ): List<CompatibilityFailure> {
    return gaeWorkedExample.question.checkHasValidHtml(origin) +
      gaeWorkedExample.explanation.checkHasValidHtml(origin)
  }

  private fun checkWrittenTranslationsCompatibility(
    origin: ContainerId,
    gaeWrittenTranslations: GaeWrittenTranslations,
    expectedContentIds: Set<String>,
    defaultLanguage: LanguageType
  ): List<CompatibilityFailure> {
    val allExpectedContentIds = expectedContentIds + gaeWrittenTranslations.translationsMapping.keys
    val contentIdLanguages = allExpectedContentIds.associateWith {
      gaeWrittenTranslations.translationsMapping[it]?.keys ?: setOf()
    }
    return gaeWrittenTranslations.translationsMapping.flatMap { (contentId, contentMap) ->
      contentMap.entries.flatMap { (languageCode, translation) ->
        val languageType = languageCode.resolveLanguageCode()
        checkWrittenTranslationCompatibility(origin, contentId, languageType, translation)
      }
    } + contentIdLanguages.flatMap { (contentId, languageCodes) ->
      languageCodes.checkHasRequiredTranslations(origin, contentId, defaultLanguage)
    }
  }

  private fun checkWrittenTranslationCompatibility(
    origin: ContainerId,
    contentId: String,
    languageType: LanguageType,
    gaeWrittenTranslation: GaeWrittenTranslation
  ): List<CompatibilityFailure> {
    return when (val translation = gaeWrittenTranslation.translation) {
      is GaeWrittenTranslation.Translation.SingleString ->
        translation.value.checkHasValidHtml(origin, contentId, languageType)
      is GaeWrittenTranslation.Translation.StringList ->
        translation.value.flatMap { it.checkHasValidHtml(origin, contentId, languageType) }
    }
  }

  private fun checkRecordedVoiceoversCompatibility(
    origin: ContainerId,
    gaeRecordedVoiceovers: GaeRecordedVoiceovers
  ): List<CompatibilityFailure> {
    return gaeRecordedVoiceovers.voiceoversMapping.values.flatMap { contentMap ->
      contentMap.values.flatMap { it.filename.checkAudioFilename(origin) }
    }
  }

  private fun checkAllEntityTranslationsCompatibility(
    origin: ContainerId,
    translations: Map<LanguageType, VersionedStructure<GaeEntityTranslations>>,
    expectedContentIds: Set<String>,
    defaultLanguage: LanguageType
  ): List<CompatibilityFailure> {
    val allExpectedContentIds =
      expectedContentIds + translations.values.flatMap { it.payload.translations.keys }
    val contentIdLanguages = allExpectedContentIds.associateWith { contentId ->
      translations.filter { (_, entityTranslation) ->
        contentId in entityTranslation.payload.translations
      }.keys
    }
    return translations.flatMap { (languageType, translations) ->
      checkEntityTranslationsCompatibility(origin, languageType, translations.payload)
    } + contentIdLanguages.flatMap { (contentId, languageCodes) ->
      languageCodes.checkHasRequiredTranslations(origin, contentId, defaultLanguage)
    }
  }

  private fun checkEntityTranslationsCompatibility(
    origin: ContainerId,
    languageType: LanguageType,
    gaeEntityTranslations: GaeEntityTranslations
  ): List<CompatibilityFailure> {
    return gaeEntityTranslations.translations.flatMap { (contentId, translatedContent) ->
      checkTranslatedContentCompatibility(origin, contentId, languageType, translatedContent)
    }
  }

  private fun checkTranslatedContentCompatibility(
    origin: ContainerId,
    contentId: String,
    languageType: LanguageType,
    gaeTranslatedContent: GaeTranslatedContent
  ): List<CompatibilityFailure> {
    return when (val translation = gaeTranslatedContent.contentValue) {
      is GaeTranslatedContent.Translation.SingleString ->
        translation.value.checkHasValidHtml(origin, contentId, languageType)
      is GaeTranslatedContent.Translation.StringList ->
        translation.value.flatMap { it.checkHasValidHtml(origin, contentId, languageType) }
    }
  }

  data class CompatibilityConstraints(
    val supportedInteractionIds: Set<String>,
    val supportedDefaultLanguages: Set<LanguageType>,
    val requiredTranslationLanguages: Set<LanguageType>,
    val supportedImageFormats: Set<String>,
    val supportedAudioFormats: Set<String>,
    val supportedHtmlTags: Set<String>,
    val supportedStateSchemaVersion: Int,
    val topicDependencies: Map<String, Set<String>>
  ) {
    fun supportsImageWithExtension(extension: String): Boolean =
      supportedImageFormats.any { it.equals(extension, ignoreCase = true) }

    fun supportsAudioWithExtension(extension: String): Boolean =
      supportedAudioFormats.any { it.equals(extension, ignoreCase = true) }

    fun hasTopicDependencies(topicId: String): Boolean = topicId in topicDependencies
  }

  sealed class CompatibilityResult {
    object Compatible : CompatibilityResult()

    data class Incompatible(val failures: List<CompatibilityFailure>) : CompatibilityResult()

    companion object {
      fun createFrom(computeFailures: () -> List<CompatibilityFailure>): CompatibilityResult =
        computeFailures().takeIf { it.isNotEmpty() }?.let { Incompatible(it) } ?: Compatible
    }
  }

  sealed class CompatibilityFailure {
    abstract val origin: ContainerId

    data class TopicHasNoKnownDependencies(
      val topicId: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class HtmlInTitleOrDescription(
      val text: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class TextHasInvalidTags(
      val contentId: String,
      val invalidTagNames: Set<String>,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class TranslatedTextHasInvalidTags(
      val contentId: String,
      val invalidTagNames: Set<String>,
      val languageType: LanguageType,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class HtmlUnexpectedlyInUnicodeContent(
      val contentId: String,
      val text: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class ThumbnailHasInvalidImageFormat(
      val imageFilename: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class TextReferencesInvalidImageFormat(
      val contentId: String,
      val imageFilename: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class TextUsesImageTagWithMissingFilePath(
      val contentId: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class ThumbnailHasInvalidColor(
      val color: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class AudioVoiceoverHasInvalidAudioFormat(
      val audioFilename: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class UnsupportedDefaultLanguageCode(
      val languageCode: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class MissingRequiredXlationLangForTitleOrDescFromWeb(
      val contentContext: LocalizationTracker.ContentContext,
      val missingLanguages: Set<LanguageType>,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class MissingRequiredXlationLangForContentTranslation(
      val contentId: String,
      val missingLanguages: Set<LanguageType>,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class StateSchemaVersionTooNew(
      val schemaVersion: Int,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class StateHasInvalidInteractionId(
      val stateName: String,
      val interactionId: String?,
      override val origin: ContainerId
    ) : CompatibilityFailure()

    data class StoryIsMissingExplorationId(
      val storyId: String,
      override val origin: ContainerId
    ) : CompatibilityFailure()
  }

  private fun String.checkIsValidTopicId(origin: ContainerId): List<CompatibilityFailure> {
    return if (!constraints.hasTopicDependencies(topicId = this)) {
      listOf(TopicHasNoKnownDependencies(topicId = this, origin))
    } else listOf()
  }

  private fun String?.checkThumbnailFilename(origin: ContainerId): List<CompatibilityFailure> {
    return this?.substringAfter('.')?.takeUnless {
      constraints.supportsImageWithExtension(it)
    }?.let { listOf(ThumbnailHasInvalidImageFormat(imageFilename = this, origin)) } ?: emptyList()
  }

  private fun String.checkImageFilename(
    origin: ContainerId,
    contentId: String
  ): List<CompatibilityFailure> {
    return substringAfter('.').takeUnless { constraints.supportsImageWithExtension(it) }?.let {
      listOf(TextReferencesInvalidImageFormat(contentId, imageFilename = this, origin))
    } ?: emptyList()
  }

  private fun String?.checkAudioFilename(origin: ContainerId): List<CompatibilityFailure> {
    return this?.substringAfter('.')?.takeUnless {
      constraints.supportsAudioWithExtension(it)
    }?.let {
      listOf(AudioVoiceoverHasInvalidAudioFormat(audioFilename = this, origin))
    } ?: emptyList()
  }

  private fun String?.checkBackgroundHexColor(origin: ContainerId): List<CompatibilityFailure> {
    return if (this != null && this.parseColorRgb() == null) {
      listOf(ThumbnailHasInvalidColor(color = this, origin))
    } else emptyList()
  }

  private fun String.checkDefaultLanguageCode(origin: ContainerId): List<CompatibilityFailure> {
    return if (resolveLanguageCode() !in constraints.supportedDefaultLanguages) {
      listOf(UnsupportedDefaultLanguageCode(languageCode = this, origin))
    } else emptyList()
  }

  @JvmName("checkLanguageCodesHaveRequiredTranslations")
  private fun Set<String>.checkHasRequiredTranslations(
    origin: ContainerId,
    contentId: String,
    defaultLanguage: LanguageType
  ): List<CompatibilityFailure> {
    return map {
      it.resolveLanguageCode()
    }.toSet().checkHasRequiredTranslations(origin, contentId, defaultLanguage)
  }

  private fun Set<LanguageType>.checkHasRequiredTranslations(
    origin: ContainerId,
    contentId: String,
    defaultLanguage: LanguageType
  ): List<CompatibilityFailure> {
    // Translations are implied for the default language (since the GAE structures embed those
    // values directly with references to content IDs).
    val availableTranslationLanguages = this + defaultLanguage
    val missingLanguages = constraints.requiredTranslationLanguages - availableTranslationLanguages
    return if (missingLanguages.isNotEmpty()) {
      listOf(MissingRequiredXlationLangForContentTranslation(contentId, missingLanguages, origin))
    } else emptyList()
  }

  private fun checkHasRequiredWebTranslationsFor(
    origin: ContainerId,
    defaultLanguage: LanguageType,
    vararg contentContexts: LocalizationTracker.ContentContext
  ): List<CompatibilityFailure> {
    return contentContexts.flatMap {
      checkHasRequiredWebTranslationsForSingleContext(origin, it, defaultLanguage)
    }
  }

  private fun checkHasRequiredWebTranslationsForSingleContext(
    origin: ContainerId,
    contentContext: LocalizationTracker.ContentContext,
    defaultLanguage: LanguageType
  ): List<CompatibilityFailure> {
    // See checkHasRequiredTranslations for the logic used here with defaultLanguage.
    val availableTranslationLanguages =
      localizationTracker.computeAvailableWebTranslations(
        origin, contentContext
      ).keys + defaultLanguage
    val missingLanguages = constraints.requiredTranslationLanguages - availableTranslationLanguages
    return if (missingLanguages.isNotEmpty()) {
      listOf(
        MissingRequiredXlationLangForTitleOrDescFromWeb(contentContext, missingLanguages, origin)
      )
    } else emptyList()
  }

  private fun GaeSubtitledHtml.checkHasValidHtml(origin: ContainerId): List<CompatibilityFailure> =
    text.checkHasValidHtml(origin, contentId, languageType = null)

  private fun GaeSubtitledUnicode.checkHasNoValidHtml(
    origin: ContainerId
  ): List<CompatibilityFailure> = text.checkUnicodeTextForHtml(origin, contentId)

  private fun String.checkHasValidHtml(
    origin: ContainerId,
    contentId: String,
    languageType: LanguageType?
  ): List<CompatibilityFailure> {
    val extraTags = extractHtmlTags() - constraints.supportedHtmlTags
    val tagFailures = if (extraTags.isNotEmpty()) {
      val failure = languageType?.let {
        TranslatedTextHasInvalidTags(contentId, extraTags, it, origin)
      } ?: TextHasInvalidTags(contentId, extraTags, origin)
      listOf(failure)
    } else emptyList()
    return tagFailures + checkHasValidImageReferences(origin, contentId)
  }

  private fun String.checkHasValidImageReferences(
    origin: ContainerId,
    contentId: String
  ): List<CompatibilityFailure> {
    val imageReferences = extractImageReferences()
    return imageReferences.filterNotNull().flatMap {
      it.checkImageFilename(origin, contentId)
    } + listOfNotNull(
      imageReferences.find { it == null }?.let {
        TextUsesImageTagWithMissingFilePath(contentId, origin)
      }
    )
  }

  private fun Int.checkIsValidStateSchemaVersion(origin: ContainerId): List<CompatibilityFailure> {
    return if (this > constraints.supportedStateSchemaVersion) {
      listOf(StateSchemaVersionTooNew(schemaVersion = this, origin))
    } else emptyList()
  }

  private fun String?.checkIsValidInteractionId(
    stateName: String,
    origin: ContainerId
  ): List<CompatibilityFailure> {
    return if (this !in constraints.supportedInteractionIds) {
      listOf(StateHasInvalidInteractionId(stateName, interactionId = this, origin))
    } else emptyList()
  }

  private companion object {
    private val HTML_PRESENCE_REGEX = "</?.+?>".toRegex()
    // This regex is a simplification of the standard: https://www.w3.org/TR/xml/#NT-NameStartChar.
    private val HTML_TAG_REGEX = "<\\s*([^\\s/>]+)[^>]*?>".toRegex()
    private val IMAGE_TAG_REGEX = "<\\s*oppia-noninteractive-image.+?>".toRegex()
    private val IMAGE_FILE_PATH_REGEX = "filepath-with-value\\s*=\\s*\"(.+?)\"".toRegex()

    private fun String.checkTitleOrDescTextForHtml(
      origin: ContainerId
    ): List<CompatibilityFailure> {
      return if (HTML_PRESENCE_REGEX.containsMatchIn(this)) {
        listOf(HtmlInTitleOrDescription(text = this, origin))
      } else emptyList()
    }

    private fun String.checkUnicodeTextForHtml(
      origin: ContainerId,
      contentId: String
    ): List<CompatibilityFailure> {
      return if (HTML_PRESENCE_REGEX.containsMatchIn(this)) {
        listOf(HtmlUnexpectedlyInUnicodeContent(contentId, text = this, origin))
      } else emptyList()
    }

    private fun String.extractHtmlTags(): Set<String> =
      HTML_TAG_REGEX.findAll(this).map { it.destructured }.map { (tagName) -> tagName }.toSet()

    // TODO: Move to common utility?
    private fun String.extractImageReferences() =
      IMAGE_TAG_REGEX.findAll(this).map { it.value.extractImageReferenceFromTag() }.toSet()

    private fun String.extractImageReferenceFromTag(): String? {
      return IMAGE_FILE_PATH_REGEX.find(this)?.destructured?.let { (filePath) ->
        filePath
      }?.removeExtraEscapedQuotes()
    }

    private fun Set<SubtitledText>.collectContentIds(): Set<String> =
      filterIsInstance<SubtitledText.TextWithContentId>().map { it.contentId }.toSet()

    // Some values are double-wrapped with quotes.
    private fun String.removeExtraEscapedQuotes() =
      removePrefix("&amp;quot;").removeSuffix("&amp;quot;")
  }
}

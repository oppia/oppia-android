package org.oppia.android.scripts.gae.compat

import org.oppia.android.scripts.gae.json.GaeCustomizationArgValue
import org.oppia.android.scripts.gae.json.GaeEntityTranslations
import org.oppia.android.scripts.gae.json.GaeExploration
import org.oppia.android.scripts.gae.json.GaeHint
import org.oppia.android.scripts.gae.json.GaeInteractionCustomizationArgsMap
import org.oppia.android.scripts.gae.json.GaeInteractionInstance
import org.oppia.android.scripts.gae.json.GaeOutcome
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
import org.oppia.android.scripts.gae.json.GaeTopic
import org.oppia.android.scripts.gae.json.GaeTranslatedContent
import org.oppia.android.scripts.gae.json.GaeWorkedExample
import org.oppia.android.scripts.gae.json.GaeWrittenTranslation
import org.oppia.android.scripts.gae.json.GaeWrittenTranslations
import org.oppia.android.scripts.gae.proto.LocalizationTracker
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContentContext.DESCRIPTION
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContentContext.TITLE
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.android.scripts.gae.json.GaeTranslatedContent.Translation.SingleString as TranslatedSingleString
import org.oppia.android.scripts.gae.json.GaeTranslatedContent.Translation.StringList as TranslatedStringList
import org.oppia.android.scripts.gae.json.GaeWrittenTranslation.Translation.SingleString as WrittenSingleString
import org.oppia.android.scripts.gae.json.GaeWrittenTranslation.Translation.StringList as WrittenStringList

class SubtitledHtmlCollector(private val localizationTracker: LocalizationTracker) {
  fun collectSubtitles(gaeTopic: GaeTopic): Set<SubtitledText> {
    val localId = LocalizationTracker.ContainerId.createFrom(gaeTopic)
    val title = setOf(gaeTopic.name.titleToSubtitle())
    val description = setOf(gaeTopic.description.descriptionToSubtitle())
    val titleXlations = localizationTracker.computeAvailableWebTranslations(localId, TITLE)
    val descXlations = localizationTracker.computeAvailableWebTranslations(localId, DESCRIPTION)
    val subtopicTexts = gaeTopic.subtopics.flatSet { it.collectSubtitles(gaeTopic.id) }
    return title + description + titleXlations.translationsToSubtitles() +
      descXlations.translationsToSubtitles() + subtopicTexts
  }

  fun collectSubtitles(gaeSubtopicPage: GaeSubtopicPage): Set<SubtitledText> {
    val mainContent = setOf(gaeSubtopicPage.pageContents.subtitledHtml.toSubtitle())
    val translations = gaeSubtopicPage.pageContents.writtenTranslations.collectSubtitles()
    return mainContent + translations
  }

  fun collectSubtitles(gaeStory: GaeStory): Set<SubtitledText> {
    val localId = LocalizationTracker.ContainerId.createFrom(gaeStory)
    val title = setOf(gaeStory.title.titleToSubtitle())
    val description = setOf(gaeStory.description.descriptionToSubtitle())
    val titleXlations = localizationTracker.computeAvailableWebTranslations(localId, TITLE)
    val descXlations = localizationTracker.computeAvailableWebTranslations(localId, DESCRIPTION)
    val chapterTexts = gaeStory.storyContents.nodes.flatSet { it.collectSubtitles(gaeStory) }
    return title + description + titleXlations.translationsToSubtitles() +
      descXlations.translationsToSubtitles() + chapterTexts
  }

  fun collectSubtitles(completeExploration: CompleteExploration): Set<SubtitledText> {
    return completeExploration.exploration.collectSubtitles() +
      completeExploration.translations.values.flatSet { it.payload.collectSubtitles() }
  }

  fun collectSubtitles(gaeSkill: GaeSkill): Set<SubtitledText> {
    val description = setOf(gaeSkill.description.descriptionToSubtitle())
    val contentTexts = gaeSkill.skillContents.collectSubtitles()
    return description + contentTexts
  }

  private fun GaeSubtopic.collectSubtitles(topicId: String): Set<SubtitledText> {
    val localId = LocalizationTracker.ContainerId.createFrom(topicId, this)
    return localizationTracker.computeAvailableWebTranslations(
      localId, TITLE
    ).translationsToSubtitles()
  }

  private fun GaeStoryNode.collectSubtitles(story: GaeStory): Set<SubtitledText> {
    val title = setOf(title.titleToSubtitle())
    val description = setOf(description.descriptionToSubtitle())
    val xlations = LocalizationTracker.ContainerId.createFrom(story, this)?.let { localId ->
      val titleXlations = localizationTracker.computeAvailableWebTranslations(localId, TITLE)
      val descXlations = localizationTracker.computeAvailableWebTranslations(localId, DESCRIPTION)
      return@let titleXlations + descXlations
    } ?: emptyMap()
    return title + description + xlations.translationsToSubtitles()
  }

  private fun GaeExploration.collectSubtitles(): Set<SubtitledText> {
    val localId = LocalizationTracker.ContainerId.createFrom(this)
    val title = setOf(title.titleToSubtitle())
    val titleXlations = localizationTracker.computeAvailableWebTranslations(localId, TITLE)
    val stateTexts = states.values.flatSet { it.collectSubtitles() }
    return title + titleXlations.translationsToSubtitles() + stateTexts
  }

  private fun GaeState.collectSubtitles(): Set<SubtitledText> =
    setOf(content.toSubtitle()) + interaction.collectSubtitles()

  private fun GaeInteractionInstance.collectSubtitles(): Set<SubtitledText> {
    val argTexts = customizationArgs.collectSubtitles()
    val groupTexts = answerGroups.flatSet { it.outcome.collectSubtitles() }
    val defaultOutcomeTexts = defaultOutcome?.collectSubtitles() ?: emptySet()
    val hintTexts = hints.flatSet { it.collectSubtitles() }
    val solutionTexts = solution?.collectSubtitles() ?: emptySet()
    return argTexts + groupTexts + defaultOutcomeTexts + hintTexts + solutionTexts
  }

  private fun GaeInteractionCustomizationArgsMap.collectSubtitles(): Set<SubtitledText> =
    customizationArgs.values.flatSet { it.collectSubtitles() }

  private fun GaeCustomizationArgValue.collectSubtitles(): Set<SubtitledText> {
    return when (this) {
      is GaeCustomizationArgValue.GaeImageWithRegions, is GaeCustomizationArgValue.SingleBoolean,
      is GaeCustomizationArgValue.SingleInteger -> emptySet()
      is GaeCustomizationArgValue.StringList -> value.mapToSet { it.customArgValueToSubtitle() }
      is GaeCustomizationArgValue.SubtitledUnicode -> setOf(value.toSubtitle())
      is GaeCustomizationArgValue.SubtitledTextList -> value.mapToSet { it.toSubtitle() }
    }
  }

  private fun GaeOutcome.collectSubtitles(): Set<SubtitledText> = setOf(feedback.toSubtitle())

  private fun GaeHint.collectSubtitles(): Set<SubtitledText> = setOf(hintContent.toSubtitle())

  private fun GaeSolution.collectSubtitles(): Set<SubtitledText> = setOf(explanation.toSubtitle())

  private fun GaeEntityTranslations.collectSubtitles(): Set<SubtitledText> =
    translations.values.flatSet { it.collectSubtitles() }

  private fun GaeTranslatedContent.collectSubtitles(): Set<SubtitledText> {
    @Suppress("USELESS_CAST") // Cast is required due to cross-module builds.
    return when (contentValue) {
      is TranslatedSingleString ->
        setOf((contentValue as TranslatedSingleString).value.translationToSubtitle())
      is TranslatedStringList ->
        (contentValue as TranslatedStringList).value.mapToSet { it.translationToSubtitle() }
    }
  }

  private fun GaeSkillContents.collectSubtitles(): Set<SubtitledText> {
    val explanationText = setOf(explanation.toSubtitle())
    val workedExampleTexts = workedExamples.flatSet { it.collectSubtitles() }
    val translations = writtenTranslations.collectSubtitles()
    return explanationText + workedExampleTexts + translations
  }

  private fun GaeWorkedExample.collectSubtitles(): Set<SubtitledText> =
    setOf(question.toSubtitle(), explanation.toSubtitle())

  private fun GaeWrittenTranslations.collectSubtitles(): Set<SubtitledText> {
    return translationsMapping.values.flatSet {
      it.values.flatSet { translation -> translation.collectSubtitles() }
    }
  }

  private fun GaeWrittenTranslation.collectSubtitles(): Set<SubtitledText> {
    // TODO: Add TODO with bug for this & other such casts by referencing https://youtrack.jetbrains.com/issue/KT-50534.
    @Suppress("USELESS_CAST") // Cast is required due to cross-module builds.
    return when (translation) {
      is WrittenSingleString ->
        setOf((translation as WrittenSingleString).value.translationToSubtitle())
      is WrittenStringList ->
        (translation as WrittenStringList).value.mapToSet { it.translationToSubtitle() }
    }
  }

  sealed class SubtitledText {
    abstract val text: String

    data class Title(override val text: String) : SubtitledText()

    data class Description(override val text: String) : SubtitledText()

    data class Translation(override val text: String) : SubtitledText()

    data class CustomizationArgValue(override val text: String) : SubtitledText()

    data class TextWithContentId(val contentId: String, override val text: String) : SubtitledText()
  }

  companion object {
    private fun <I, O> Iterable<I>.flatSet(transform: (I) -> Set<O>): Set<O> =
      flatMapTo(mutableSetOf(), transform)

    private fun String.titleToSubtitle() = SubtitledText.Title(this)

    private fun String.descriptionToSubtitle() = SubtitledText.Description(this)

    private fun String.translationToSubtitle() = SubtitledText.Translation(this)

    private fun Map<LanguageType, String>.translationsToSubtitles() =
      values.mapToSet { it.translationToSubtitle() }

    private fun String.customArgValueToSubtitle() = SubtitledText.CustomizationArgValue(this)

    private fun GaeSubtitledHtml.toSubtitle() = SubtitledText.TextWithContentId(contentId, text)

    private fun GaeSubtitledUnicode.toSubtitle() = SubtitledText.TextWithContentId(contentId, text)

    private fun <I, O> Iterable<I>.mapToSet(transform: (I) -> O): Set<O> =
      mapTo(mutableSetOf(), transform)
  }
}

package org.oppia.android.scripts.gae.proto

import org.oppia.android.scripts.gae.json.GaeAnswerGroup
import org.oppia.android.scripts.gae.json.GaeClassroom
import org.oppia.android.scripts.gae.json.GaeCustomizationArgValue
import org.oppia.android.scripts.gae.json.GaeCustomizationArgValue.GaeImageWithRegions
import org.oppia.android.scripts.gae.json.GaeCustomizationArgValue.GaeImageWithRegions.GaeLabeledRegion
import org.oppia.android.scripts.gae.json.GaeCustomizationArgValue.GaeImageWithRegions.GaeLabeledRegion.GaeNormalizedRectangle2d
import org.oppia.android.scripts.gae.json.GaeEntityTranslations
import org.oppia.android.scripts.gae.json.GaeExploration
import org.oppia.android.scripts.gae.json.GaeHint
import org.oppia.android.scripts.gae.json.GaeInteractionInstance
import org.oppia.android.scripts.gae.json.GaeInteractionObject
import org.oppia.android.scripts.gae.json.GaeInteractionObject.Fraction
import org.oppia.android.scripts.gae.json.GaeInteractionObject.MathExpression
import org.oppia.android.scripts.gae.json.GaeInteractionObject.NonNegativeInt
import org.oppia.android.scripts.gae.json.GaeInteractionObject.NormalizedString
import org.oppia.android.scripts.gae.json.GaeInteractionObject.RatioExpression
import org.oppia.android.scripts.gae.json.GaeInteractionObject.Real
import org.oppia.android.scripts.gae.json.GaeInteractionObject.SetOfXlatableContentIds
import org.oppia.android.scripts.gae.json.GaeInteractionObject.SetsOfXlatableContentIds
import org.oppia.android.scripts.gae.json.GaeInteractionObject.SignedInt
import org.oppia.android.scripts.gae.json.GaeInteractionObject.TranslatableHtmlContentId
import org.oppia.android.scripts.gae.json.GaeInteractionObject.TranslatableSetOfNormalizedString
import org.oppia.android.scripts.gae.json.GaeOutcome
import org.oppia.android.scripts.gae.json.GaeRuleSpec
import org.oppia.android.scripts.gae.json.GaeSkill
import org.oppia.android.scripts.gae.json.GaeSolution
import org.oppia.android.scripts.gae.json.GaeState
import org.oppia.android.scripts.gae.json.GaeStory
import org.oppia.android.scripts.gae.json.GaeStoryNode
import org.oppia.android.scripts.gae.json.GaeSubtitledHtml
import org.oppia.android.scripts.gae.json.GaeSubtitledUnicode
import org.oppia.android.scripts.gae.json.GaeSubtopic
import org.oppia.android.scripts.gae.json.GaeSubtopicPage
import org.oppia.android.scripts.gae.json.GaeTopic
import org.oppia.android.scripts.gae.json.GaeWorkedExample
import org.oppia.android.scripts.gae.json.VersionedStructure
import org.oppia.android.scripts.gae.proto.LocalizationTracker.Companion.resolveLanguageCode
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContentContext.DESCRIPTION
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContentContext.TITLE
import org.oppia.android.scripts.gae.proto.SolutionAnswer.AnswerTypeCase.FRACTION
import org.oppia.android.scripts.gae.proto.SolutionAnswer.AnswerTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS
import org.oppia.android.scripts.gae.proto.SolutionAnswer.AnswerTypeCase.MATH_EXPRESSION
import org.oppia.android.scripts.gae.proto.SolutionAnswer.AnswerTypeCase.NORMALIZED_STRING
import org.oppia.android.scripts.gae.proto.SolutionAnswer.AnswerTypeCase.RATIO_EXPRESSION
import org.oppia.android.scripts.gae.proto.SolutionAnswer.AnswerTypeCase.REAL
import org.oppia.proto.v1.structure.AlgebraicExpressionInputInstanceDto
import org.oppia.proto.v1.structure.BaseAnswerGroupDto
import org.oppia.proto.v1.structure.BaseSolutionDto
import org.oppia.proto.v1.structure.ChapterSummaryDto
import org.oppia.proto.v1.structure.ClassroomDto
import org.oppia.proto.v1.structure.ConceptCardDto
import org.oppia.proto.v1.structure.ConceptCardDto.WorkedExampleDto
import org.oppia.proto.v1.structure.ConceptCardLanguagePackDto
import org.oppia.proto.v1.structure.ContinueInstanceDto
import org.oppia.proto.v1.structure.DownloadableTopicSummaryDto
import org.oppia.proto.v1.structure.DragAndDropSortInputInstanceDto
import org.oppia.proto.v1.structure.EndExplorationInstanceDto
import org.oppia.proto.v1.structure.ExplorationDto
import org.oppia.proto.v1.structure.ExplorationLanguagePackDto
import org.oppia.proto.v1.structure.FractionDto
import org.oppia.proto.v1.structure.FractionInputInstanceDto
import org.oppia.proto.v1.structure.HintDto
import org.oppia.proto.v1.structure.ImageClickInputInstanceDto
import org.oppia.proto.v1.structure.ImageWithRegionsDto
import org.oppia.proto.v1.structure.ImageWithRegionsDto.LabeledRegionDto
import org.oppia.proto.v1.structure.ImageWithRegionsDto.LabeledRegionDto.NormalizedRectangle2dDto
import org.oppia.proto.v1.structure.InteractionInstanceDto
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.ALGEBRAIC_EXPRESSION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.CONTINUE_INSTANCE
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.DRAG_AND_DROP_SORT_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.END_EXPLORATION
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.FRACTION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.IMAGE_CLICK_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.INTERACTIONTYPE_NOT_SET
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.ITEM_SELECTION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.MATH_EQUATION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.MULTIPLE_CHOICE_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.NUMERIC_EXPRESSION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.NUMERIC_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.RATIO_EXPRESSION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.TEXT_INPUT
import org.oppia.proto.v1.structure.ItemSelectionInputInstanceDto
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.proto.v1.structure.ListOfSetsOfTranslatableHtmlContentIdsDto
import org.oppia.proto.v1.structure.LocalizedConceptCardIdDto
import org.oppia.proto.v1.structure.LocalizedExplorationIdDto
import org.oppia.proto.v1.structure.LocalizedRevisionCardIdDto
import org.oppia.proto.v1.structure.MathEquationInputInstanceDto
import org.oppia.proto.v1.structure.MultipleChoiceInputInstanceDto
import org.oppia.proto.v1.structure.NormalizedPoint2dDto
import org.oppia.proto.v1.structure.NumericExpressionInputInstanceDto
import org.oppia.proto.v1.structure.NumericInputInstanceDto
import org.oppia.proto.v1.structure.OutcomeDto
import org.oppia.proto.v1.structure.RatioExpressionDto
import org.oppia.proto.v1.structure.RatioExpressionInputInstanceDto
import org.oppia.proto.v1.structure.RevisionCardDto
import org.oppia.proto.v1.structure.RevisionCardLanguagePackDto
import org.oppia.proto.v1.structure.SetOfTranslatableHtmlContentIdsDto
import org.oppia.proto.v1.structure.SkillSummaryDto
import org.oppia.proto.v1.structure.StateDto
import org.oppia.proto.v1.structure.StorySummaryDto
import org.oppia.proto.v1.structure.SubtitledTextDto
import org.oppia.proto.v1.structure.SubtopicPageIdDto
import org.oppia.proto.v1.structure.SubtopicSummaryDto
import org.oppia.proto.v1.structure.TextInputInstanceDto
import org.oppia.proto.v1.structure.TranslatableHtmlContentIdDto
import org.oppia.proto.v1.structure.TranslatableSetOfNormalizedStringDto
import org.oppia.proto.v1.structure.UpcomingTopicSummaryDto
import org.oppia.android.scripts.gae.json.GaeInteractionCustomizationArgsMap as GaeInteractionArgsMap
import org.oppia.android.scripts.gae.proto.RuleInputType.InputTypeCase as RuleInputTypeCase
import org.oppia.proto.v1.structure.AlgebraicExpressionInputInstanceDto.AnswerGroupDto as AlgebraicExpAnswerGroupDto
import org.oppia.proto.v1.structure.AlgebraicExpressionInputInstanceDto.RuleSpecDto.IsEquivalentToSpecDto as AlgebraicExpressionIsEquivalentSpec
import org.oppia.proto.v1.structure.AlgebraicExpressionInputInstanceDto.RuleSpecDto.MatchesExactlyWithSpecDto as AlgebraicExpressionMatchesExactlySpec
import org.oppia.proto.v1.structure.AlgebraicExpressionInputInstanceDto.RuleSpecDto.MatchesUpToTrivialManipulationsSpecDto as AlgebraicExpressionTrivialManipsSpec
import org.oppia.proto.v1.structure.DragAndDropSortInputInstanceDto.AnswerGroupDto as DragAndDropAnswerGroupDto
import org.oppia.proto.v1.structure.DragAndDropSortInputInstanceDto.RuleSpecDto.HasElementXAtPositionYSpecDto as DragAndDropHasElementXAtPositionYSpec
import org.oppia.proto.v1.structure.DragAndDropSortInputInstanceDto.RuleSpecDto.HasElementXBeforeElementYSpecDto as DragAndDropHasElementXBeforeElementYSpec
import org.oppia.proto.v1.structure.DragAndDropSortInputInstanceDto.RuleSpecDto.IsEqualToOrderingSpecDto as DragAndDropIsEqualToOrderingSpec
import org.oppia.proto.v1.structure.DragAndDropSortInputInstanceDto.RuleSpecDto.IsEqualToOrderingWithOneItemAtIncorrectPositionSpecDto as DragAndDropIsEqualToOrderingWithOneItemAtIncorrectPositionSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.HasDenominatorEqualToSpecDto as FractionHasDenominatorEqualToSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.HasFractionalPartExactlyEqualToSpecDto as FractionHasFractionalPartExactlyEqualToSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.HasIntegerPartEqualToSpecDto as FractionHasIntegerPartEqualToSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.HasNoFractionalPartSpecDto as FractionHasNoFractionalPartSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.HasNumeratorEqualToSpecDto as FractionHasNumeratorEqualToSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.IsEquivalentToAndInSimplestFormSpecDto as FractionIsEquivalentToAndInSimplestFormSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.IsEquivalentToSpecDto as FractionIsEquivalentToSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.IsExactlyEqualToSpecDto as FractionIsExactlyEqualToSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.IsGreaterThanSpecDto as FractionIsGreaterThanSpec
import org.oppia.proto.v1.structure.FractionInputInstanceDto.RuleSpecDto.IsLessThanSpecDto as FractionIsLessThanSpec
import org.oppia.proto.v1.structure.ImageClickInputInstanceDto.AnswerGroupDto as ImageClickAnswerGroupDto
import org.oppia.proto.v1.structure.ImageClickInputInstanceDto.RuleSpecDto.IsInRegionSpecDto as ImageClickIsInRegionSpec
import org.oppia.proto.v1.structure.ItemSelectionInputInstanceDto.AnswerGroupDto as ItemSelectionAnswerGroupDto
import org.oppia.proto.v1.structure.ItemSelectionInputInstanceDto.RuleSpecDto.ContainsAtLeastOneOfSpecDto as ItemSelectionContainsAtLeastOneOfSpec
import org.oppia.proto.v1.structure.ItemSelectionInputInstanceDto.RuleSpecDto.DoesNotContainAtLeastOneOfSpecDto as ItemSelectionDoesNotContainAtLeastOneOfSpec
import org.oppia.proto.v1.structure.ItemSelectionInputInstanceDto.RuleSpecDto.EqualsSpecDto as ItemSelectionEqualsSpec
import org.oppia.proto.v1.structure.ItemSelectionInputInstanceDto.RuleSpecDto.IsProperSubsetOfSpecDto as ItemSelectionIsProperSubsetOfSpec
import org.oppia.proto.v1.structure.MathEquationInputInstanceDto.AnswerGroupDto as MathEquationAnswerGroupDto
import org.oppia.proto.v1.structure.MathEquationInputInstanceDto.CustomizationArgsDto as MathEquationCustomizationArgsDto
import org.oppia.proto.v1.structure.MathEquationInputInstanceDto.RuleSpecDto as MathEquationRuleSpecDto
import org.oppia.proto.v1.structure.MathEquationInputInstanceDto.RuleSpecDto.IsEquivalentToSpecDto as MathEquationIsEquivalentSpec
import org.oppia.proto.v1.structure.MathEquationInputInstanceDto.RuleSpecDto.MatchesExactlyWithSpecDto as MathEquationMatchesExactlySpec
import org.oppia.proto.v1.structure.MathEquationInputInstanceDto.RuleSpecDto.MatchesUpToTrivialManipulationsSpecDto as MathEquationTrivialManipsSpec
import org.oppia.proto.v1.structure.MultipleChoiceInputInstanceDto.AnswerGroupDto as MultipleChoiceAnswerGroupDto
import org.oppia.proto.v1.structure.MultipleChoiceInputInstanceDto.RuleSpecDto.EqualsSpecDto as MultipleChoiceEqualsSpec
import org.oppia.proto.v1.structure.NumericExpressionInputInstanceDto.AnswerGroupDto as NumericExpressionAnswerGroupDto
import org.oppia.proto.v1.structure.NumericExpressionInputInstanceDto.RuleSpecDto.IsEquivalentToSpecDto as NumericExpressionIsEquivalentSpec
import org.oppia.proto.v1.structure.NumericExpressionInputInstanceDto.RuleSpecDto.MatchesExactlyWithSpecDto as NumericExpressionMatchesExactlySpec
import org.oppia.proto.v1.structure.NumericExpressionInputInstanceDto.RuleSpecDto.MatchesUpToTrivialManipulationsSpecDto as NumericExpressionTrivialManipsSpec
import org.oppia.proto.v1.structure.NumericInputInstanceDto.RuleSpecDto.EqualsSpecDto as NumericEqualsSpec
import org.oppia.proto.v1.structure.NumericInputInstanceDto.RuleSpecDto.IsGreaterThanOrEqualToSpecDto as NumericIsGreaterThanOrEqualToSpec
import org.oppia.proto.v1.structure.NumericInputInstanceDto.RuleSpecDto.IsGreaterThanSpecDto as NumericIsGreaterThanSpec
import org.oppia.proto.v1.structure.NumericInputInstanceDto.RuleSpecDto.IsInclusivelyBetweenSpecDto as NumericIsInclusivelyBetweenSpec
import org.oppia.proto.v1.structure.NumericInputInstanceDto.RuleSpecDto.IsLessThanOrEqualToSpecDto as NumericIsLessThanOrEqualToSpec
import org.oppia.proto.v1.structure.NumericInputInstanceDto.RuleSpecDto.IsLessThanSpecDto as NumericIsLessThanSpec
import org.oppia.proto.v1.structure.NumericInputInstanceDto.RuleSpecDto.IsWithinToleranceSpecDto as NumericIsWithinToleranceSpec
import org.oppia.proto.v1.structure.RatioExpressionInputInstanceDto.AnswerGroupDto as RatioExpressionAnswerGroupDto
import org.oppia.proto.v1.structure.RatioExpressionInputInstanceDto.RuleSpecDto.EqualsSpecDto as RatioEqualsSpec
import org.oppia.proto.v1.structure.RatioExpressionInputInstanceDto.RuleSpecDto.HasNumberOfTermsEqualToSpecDto as RatioHasNumberOfTermsEqualToSpec
import org.oppia.proto.v1.structure.RatioExpressionInputInstanceDto.RuleSpecDto.HasSpecificTermEqualToSpecDto as RatioHasSpecificTermEqualToSpec
import org.oppia.proto.v1.structure.RatioExpressionInputInstanceDto.RuleSpecDto.IsEquivalentSpecDto as RatioIsEquivalentSpec
import org.oppia.proto.v1.structure.TextInputInstanceDto.RuleSpecDto.ContainsSpecDto as TextContainsSpec
import org.oppia.proto.v1.structure.TextInputInstanceDto.RuleSpecDto.EqualsSpecDto as TextEqualsSpec
import org.oppia.proto.v1.structure.TextInputInstanceDto.RuleSpecDto.FuzzyEqualsSpecDto as TextFuzzyEqualsSpec
import org.oppia.proto.v1.structure.TextInputInstanceDto.RuleSpecDto.StartsWithSpecDto as TextStartsWithSpec

class JsonToProtoConverter(
  private val localizationTracker: LocalizationTracker,
  private val topicDependencies: Map<String, Set<String>>
) {
  fun trackClassroomTranslations(classrooms: List<GaeClassroom>) {
    for (classroom in classrooms) {
      val containerId = LocalizationTracker.ContainerId.createFrom(classroom)
      // TODO: Classrooms don't have a language code exposed.
      val defaultLanguage = LanguageType.ENGLISH
      // TODO: Add missing thumbnail once it's available.
      localizationTracker.initializeContainer(containerId, defaultLanguage)
      localizationTracker.trackContainerText(containerId, TITLE, classroom.name)
    }
  }

  fun trackTopicTranslations(topics: Map<String, GaeTopic>) {
    for (topic in topics.values) {
      val containerId = LocalizationTracker.ContainerId.createFrom(topic)
      localizationTracker.initializeContainer(containerId, topic.languageCode.resolveLanguageCode())
      localizationTracker.trackThumbnail(
        containerId, topic.thumbnailFilename, topic.thumbnailBgColor, topic.thumbnailSizeInBytes
      )
      localizationTracker.trackContainerText(containerId, TITLE, topic.name)
      localizationTracker.trackContainerText(containerId, DESCRIPTION, topic.description)
    }
  }

  fun trackStoryTranslations(stories: Map<String, GaeStory>) {
    for (story in stories.values) {
      val containerId = LocalizationTracker.ContainerId.createFrom(story)
      val defaultLanguage = story.languageCode.resolveLanguageCode()
      localizationTracker.initializeContainer(containerId, defaultLanguage)
      localizationTracker.trackThumbnail(
        containerId, story.thumbnailFilename, story.thumbnailBgColor, story.thumbnailSizeInBytes
      )
      localizationTracker.trackContainerText(containerId, TITLE, story.title)
      localizationTracker.trackContainerText(containerId, DESCRIPTION, story.description)

      for (storyNode in story.storyContents.nodes) {
        val nodeContainerId =
          checkNotNull(LocalizationTracker.ContainerId.createFrom(story, storyNode)) {
            "Story node doesn't have an exploration ID: $storyNode. Cannot convert to proto."
          }
        localizationTracker.initializeContainer(nodeContainerId, defaultLanguage)
        localizationTracker.trackThumbnail(
          nodeContainerId,
          storyNode.thumbnailFilename,
          storyNode.thumbnailBgColor,
          storyNode.thumbnailSizeInBytes
        )
        localizationTracker.trackContainerText(nodeContainerId, TITLE, storyNode.title)
        localizationTracker.trackContainerText(nodeContainerId, DESCRIPTION, storyNode.outline)
      }
    }
  }

  fun trackExplorationTranslations(explorations: Map<String, ExplorationPackage>) {
    for ((exploration, allTranslations) in explorations.values) {
      val containerId = LocalizationTracker.ContainerId.createFrom(exploration)
      val defaultLanguage = exploration.languageCode.resolveLanguageCode()
      localizationTracker.initializeContainer(containerId, defaultLanguage)
      localizationTracker.trackContainerText(containerId, TITLE, exploration.title)

      // Track all subtitled text in each state of the exploration.
      exploration.states.values.flatMap { state ->
        state.interaction.answerGroups.map { answerGroup ->
          answerGroup.outcome.feedback
        } + state.interaction.hints.map { hint ->
          hint.hintContent
        } + state.interaction.customizationArgs.customizationArgs.flatMap { (_, argVal) ->
          when (argVal) {
            is GaeImageWithRegions, is GaeCustomizationArgValue.SingleBoolean,
            is GaeCustomizationArgValue.SingleInteger, is GaeCustomizationArgValue.StringList ->
              emptyList()
            is GaeCustomizationArgValue.SubtitledTextList -> argVal.value
            is GaeCustomizationArgValue.SubtitledUnicode -> listOf(argVal.value)
          }
        } + listOfNotNull(
          state.content,
          state.interaction.defaultOutcome?.feedback,
          state.interaction.solution?.explanation
        )
      }.forEach { localizationTracker.trackContainerText(containerId, it) }

      // Voiceovers are only tracked after their corresponding content IDs have already been
      // properly defaulted.
      for (state in exploration.states.values) {
        localizationTracker.trackVoiceovers(containerId, state.recordedVoiceovers)
      }

      // Track all translatable answer inputs.
      exploration.states.values.flatMap { state ->
        state.interaction.answerGroups.flatMap { answerGroup ->
          answerGroup.ruleSpecs.flatMap { ruleSpec ->
            ruleSpec.inputs.values.mapNotNull { ruleInput ->
              when (ruleInput) {
                // Note that translatable content IDs objects are ignored because they don't provide
                // new translations and should already be tracked in the interaction's customization
                // arguments.
                is Fraction, is MathExpression, is NonNegativeInt, is NormalizedString,
                is RatioExpression, is Real, is SignedInt, is SetOfXlatableContentIds,
                is SetsOfXlatableContentIds, is TranslatableHtmlContentId -> null
                is TranslatableSetOfNormalizedString ->
                  ruleInput.contentId?.let { it to ruleInput.normalizedStrSet }
              }
            }
          }
        }
      }.forEach { (contentId, strList) ->
        localizationTracker.trackContainerText(containerId, contentId, strList)
      }

      // Track translations after all default strings have been established.
      for ((language, translations) in allTranslations) {
        localizationTracker.trackTranslations(containerId, language, translations.payload)
      }
    }
  }

  fun trackConceptCardTranslations(skills: Map<String, GaeSkill>) {
    for (skill in skills.values) {
      val defaultLanguage = skill.languageCode.resolveLanguageCode()

      // TODO: Skills do not currently have a name defined.
      val skillContainerId = LocalizationTracker.ContainerId.Skill(skill.id)
      localizationTracker.initializeContainer(skillContainerId, defaultLanguage)
      localizationTracker.trackContainerText(
        skillContainerId, TITLE, "<auto_skill_name_${skill.id}>"
      )

      // TODO: Oppia web doesn't have description translations for concept cards.
      val conceptCardContainerId = LocalizationTracker.ContainerId.createFrom(skill)
      val contents = skill.skillContents
      localizationTracker.initializeContainer(conceptCardContainerId, defaultLanguage)
      localizationTracker.trackContainerText(conceptCardContainerId, contents.explanation)
      for (workedExample in contents.workedExamples) {
        localizationTracker.trackContainerText(conceptCardContainerId, workedExample.question)
        localizationTracker.trackContainerText(conceptCardContainerId, workedExample.explanation)
      }

      // Track translations after all default strings have been established.
      localizationTracker.trackTranslations(conceptCardContainerId, contents.writtenTranslations)
    }
  }

  fun trackRevisionCardTranslations(revisionCards: List<Pair<GaeSubtopic, GaeSubtopicPage>>) {
    for ((subtopic, subtopicPage) in revisionCards) {
      val containerId = LocalizationTracker.ContainerId.createFrom(subtopicPage, subtopic)
      val defaultLanguage = subtopicPage.languageCode.resolveLanguageCode()
      val pageContents = subtopicPage.pageContents
      localizationTracker.initializeContainer(containerId, defaultLanguage)
      localizationTracker.trackContainerText(containerId, TITLE, subtopic.title)
      localizationTracker.trackContainerText(containerId, pageContents.subtitledHtml)
      localizationTracker.trackThumbnail(
        containerId,
        subtopic.thumbnailFilename,
        subtopic.thumbnailBgColor,
        subtopic.thumbnailSizeInBytes
      )

      // Track translations after all default strings have been established.
      localizationTracker.trackTranslations(containerId, pageContents.writtenTranslations)
    }
  }

  suspend fun convertToClassroom(
    gaeClassroom: GaeClassroom,
    defaultLanguage: LanguageType
  ): ClassroomDto {
    val containerId = LocalizationTracker.ContainerId.createFrom(gaeClassroom)
    return ClassroomDto.newBuilder().apply {
      this.protoVersion = ProtoVersionProvider.createLatestClassroomProtoVersion()
      this.id = gaeClassroom.id
      this.name = localizationTracker.convertContainerText(containerId, TITLE)
      addAllTopicIds(gaeClassroom.topicIdToPrereqTopicIds.keys)
      this.localizations =
        localizationTracker.computeCompleteLocalizationPack(containerId, defaultLanguage)
    }.build()
  }

  suspend fun convertToDownloadableTopicSummary(
    gaeTopic: GaeTopic,
    defaultLanguage: LanguageType,
    subtopicPages: Map<SubtopicPageIdDto, GaeSubtopicPage>,
    stories: Map<String, GaeStory>,
    explorations: Map<String, ExplorationPackage>,
    referencedSkills: Map<String, GaeSkill>
  ): DownloadableTopicSummaryDto {
    return DownloadableTopicSummaryDto.newBuilder().apply {
      val containerId = LocalizationTracker.ContainerId.createFrom(gaeTopic)
      val storySummaries =
        gaeTopic.computeReferencedStoryIds().map { storyId ->
          stories.getValue(storyId).toProto(defaultLanguage, explorations)
        }
      val subtopicSummaries =
        gaeTopic.subtopics.map { subtopic ->
          val subtopicPageId = SubtopicPageIdDto.newBuilder().apply {
            this.topicId = gaeTopic.id
            this.subtopicIndex = subtopic.id
          }.build()
          subtopic.toProto(subtopicPages.getValue(subtopicPageId))
        }
      val skillSummaries = referencedSkills.map { (skillId, gaeSkill) ->
        SkillSummaryDto.newBuilder().apply {
          val skillContainerId = LocalizationTracker.ContainerId.Skill(skillId)
          this.id = skillId
          this.name = localizationTracker.convertContainerText(skillContainerId, TITLE)
          this.contentVersion = gaeSkill.version
          this.localizations =
            localizationTracker.computeCompleteLocalizationPack(skillContainerId, defaultLanguage)
        }.build()
      }

      this.protoVersion = ProtoVersionProvider.createLatestTopicSummaryProtoVersion()
      this.id = gaeTopic.id
      this.name = localizationTracker.convertContainerText(containerId, TITLE)
      this.description = localizationTracker.convertContainerText(containerId, DESCRIPTION)
      this.addAllStorySummaries(storySummaries)
      this.addAllSubtopicSummaries(subtopicSummaries)
      this.contentVersion = gaeTopic.version
      this.addAllPrerequisiteTopicIds(computeTopicDependencies(gaeTopic.id))
      this.localizations =
        localizationTracker.computeCompleteLocalizationPack(containerId, defaultLanguage)
      this.addAllReferencedSkills(skillSummaries)
    }.build()
  }

  suspend fun convertToUpcomingTopicSummary(
    gaeTopic: GaeTopic,
    defaultLanguage: LanguageType
  ): UpcomingTopicSummaryDto {
    return UpcomingTopicSummaryDto.newBuilder().apply {
      val containerId = LocalizationTracker.ContainerId.createFrom(gaeTopic)
      this.protoVersion = ProtoVersionProvider.createLatestTopicSummaryProtoVersion()
      this.id = gaeTopic.id
      this.name = localizationTracker.convertContainerText(containerId, TITLE)
      this.description = localizationTracker.convertContainerText(containerId, DESCRIPTION)
      this.localizations =
        localizationTracker.computeCompleteLocalizationPack(containerId, defaultLanguage)
      // No anticipated prerequisite topic IDs or anticipated release time.
    }.build()
  }

  suspend fun convertToRevisionCard(
    gaeSubtopicPage: GaeSubtopicPage,
    gaeSubtopic: GaeSubtopic,
    defaultLanguage: LanguageType
  ): RevisionCardDto {
    return RevisionCardDto.newBuilder().apply {
      val containerId = LocalizationTracker.ContainerId.createFrom(gaeSubtopicPage, gaeSubtopic)
      this.protoVersion = ProtoVersionProvider.createLatestRevisionCardProtoVersion()
      this.id = SubtopicPageIdDto.newBuilder().apply {
        this.topicId = gaeSubtopicPage.topicId
        this.subtopicIndex = gaeSubtopic.id
      }.build()
      this.title = localizationTracker.convertContainerText(containerId, TITLE)
      this.content =
        localizationTracker.convertContainerText(
          containerId, gaeSubtopicPage.pageContents.subtitledHtml
        )
      this.defaultLocalization =
        localizationTracker.computeSpecificContentLocalization(containerId, defaultLanguage)
      this.contentVersion = gaeSubtopicPage.version
    }.build()
  }

  suspend fun convertToConceptCard(
    gaeSkill: GaeSkill,
    defaultLanguage: LanguageType
  ): ConceptCardDto {
    return ConceptCardDto.newBuilder().apply {
      val containerId = LocalizationTracker.ContainerId.createFrom(gaeSkill)
      this.protoVersion = ProtoVersionProvider.createLatestConceptCardProtoVersion()
      this.skillId = gaeSkill.id
      this.explanation =
        localizationTracker.convertContainerText(containerId, gaeSkill.skillContents.explanation)
      this.addAllWorkedExamples(
        gaeSkill.skillContents.workedExamples.map { it.toProto(containerId) }
      )
      this.defaultLocalization =
        localizationTracker.computeSpecificContentLocalization(containerId, defaultLanguage)
      this.contentVersion = gaeSkill.version
    }.build()
  }

  suspend fun convertToExploration(
    gaeExploration: GaeExploration,
    defaultLanguage: LanguageType
  ): ExplorationDto {
    return ExplorationDto.newBuilder().apply {
      val containerId = LocalizationTracker.ContainerId.createFrom(gaeExploration)
      this.protoVersion = ProtoVersionProvider.createLatestExplorationProtoVersion()
      this.id = gaeExploration.id
      this.title = localizationTracker.convertContainerText(containerId, TITLE)
      this.initStateName = gaeExploration.initStateName
      this.putAllStates(
        gaeExploration.states.mapValues { (_, state) -> state.toProto(containerId) }
      )
      this.contentVersion = gaeExploration.version
      this.defaultLocalization =
        localizationTracker.computeSpecificContentLocalization(containerId, defaultLanguage)
    }.build()
  }

  suspend fun retrieveRevisionCardLanguagePack(
    id: LocalizedRevisionCardIdDto,
    gaeSubtopic: GaeSubtopic,
    gaeSubtopicPage: GaeSubtopicPage
  ): RevisionCardLanguagePackDto {
    return RevisionCardLanguagePackDto.newBuilder().apply {
      val containerId = LocalizationTracker.ContainerId.createFrom(id, gaeSubtopic)
      this.protoVersion = ProtoVersionProvider.createLatestRevisionCardProtoVersion()
      this.id = id
      this.localization =
        localizationTracker.computeSpecificContentLocalization(containerId, id.language)
      // Translations are embedded within revision cards, so the pack's version is always the same
      // as the revision card's version.
      this.contentVersion = gaeSubtopicPage.version
    }.build()
  }

  suspend fun retrieveConceptCardLanguagePack(
    id: LocalizedConceptCardIdDto,
    gaeSkill: GaeSkill
  ): ConceptCardLanguagePackDto {
    return ConceptCardLanguagePackDto.newBuilder().apply {
      val containerId = LocalizationTracker.ContainerId.createFrom(id)
      this.protoVersion = ProtoVersionProvider.createLatestConceptCardProtoVersion()
      this.id = id
      this.localization =
        localizationTracker.computeSpecificContentLocalization(containerId, id.language)
      // Translations are embedded within concept card cards, so the pack's version is always the
      // same as the concept card's version.
      this.contentVersion = gaeSkill.version
    }.build()
  }

  suspend fun convertToExplorationLanguagePack(
    id: LocalizedExplorationIdDto,
    contentVersion: Int
  ): ExplorationLanguagePackDto {
    return ExplorationLanguagePackDto.newBuilder().apply {
      val containerId = LocalizationTracker.ContainerId.createFrom(id)
      this.protoVersion = ProtoVersionProvider.createLatestExplorationProtoVersion()
      this.id = id
      this.localization =
        localizationTracker.computeSpecificContentLocalization(containerId, id.language)
      this.contentVersion = contentVersion
    }.build()
  }

  data class ExplorationPackage(
    val exploration: GaeExploration,
    val translations: Map<LanguageType, VersionedStructure<GaeEntityTranslations>>
  )

  private fun GaeWorkedExample.toProto(
    containerId: LocalizationTracker.ContainerId
  ): WorkedExampleDto? {
    return WorkedExampleDto.newBuilder().apply {
      this.question = localizationTracker.convertContainerText(containerId, this@toProto.question)
      this.explanation =
        localizationTracker.convertContainerText(containerId, this@toProto.explanation)
    }.build()
  }

  private suspend fun GaeStory.toProto(
    defaultLanguage: LanguageType,
    availableExplorations: Map<String, ExplorationPackage>
  ): StorySummaryDto {
    return StorySummaryDto.newBuilder().apply {
      val containerId = LocalizationTracker.ContainerId.createFrom(this@toProto)
      val chapterSummaries =
        this@toProto.storyContents.nodes.map {
          val expPackage = availableExplorations.getValue(it.expectedExplorationId)
          it.toProto(this@toProto, expPackage, defaultLanguage)
        }

      this.id = this@toProto.id
      this.title = localizationTracker.convertContainerText(containerId, TITLE)
      this.description = localizationTracker.convertContainerText(containerId, DESCRIPTION)
      this.addAllChapters(chapterSummaries)
      this.contentVersion = this@toProto.version
      this.localizations =
        localizationTracker.computeCompleteLocalizationPack(containerId, defaultLanguage)
    }.build()
  }

  private suspend fun GaeStoryNode.toProto(
    containingStory: GaeStory,
    matchingExploration: ExplorationPackage,
    defaultLanguage: LanguageType
  ): ChapterSummaryDto {
    return ChapterSummaryDto.newBuilder().apply {
      val containerId =
        checkNotNull(LocalizationTracker.ContainerId.createFrom(containingStory, this@toProto)) {
          "Story node doesn't have an exploration ID: $containingStory. Cannot convert to proto."
        }
      this.title = localizationTracker.convertContainerText(containerId, TITLE)
      this.description = localizationTracker.convertContainerText(containerId, DESCRIPTION)
      this.explorationId = matchingExploration.exploration.id
      this.contentVersion = matchingExploration.exploration.version
      this.localizations =
        localizationTracker.computeCompleteLocalizationPack(containerId, defaultLanguage)
    }.build()
  }

  private fun GaeSubtopic.toProto(matchingSubtopicPage: GaeSubtopicPage): SubtopicSummaryDto {
    return SubtopicSummaryDto.newBuilder().apply {
      this.index = this@toProto.id
      this.addAllReferencedSkillIds(this@toProto.skillIds)
      this.contentVersion = matchingSubtopicPage.version
    }.build()
  }

  private fun GaeState.toProto(containerId: LocalizationTracker.ContainerId): StateDto {
    return StateDto.newBuilder().apply {
      this.protoVersion = ProtoVersionProvider.createLatestStateProtoVersion()
      this.content = this@toProto.content.toProto(containerId)
      this.interaction = this@toProto.interaction.toProto(containerId)
    }.build()
  }

  private fun GaeInteractionInstance.toProto(
    containerId: LocalizationTracker.ContainerId
  ): InteractionInstanceDto {
    return InteractionInstanceDto.newBuilder().apply {
      when (id) {
        "Continue" -> this.continueInstance = toContinueInstance(containerId)
        "FractionInput" -> this.fractionInput = toFractionInputInstance(containerId)
        "ItemSelectionInput" -> this.itemSelectionInput = toItemSelectionInputInstance(containerId)
        "MultipleChoiceInput" ->
          this.multipleChoiceInput = toMultipleChoiceInputInstance(containerId)
        "NumericInput" -> this.numericInput = toNumericInputInstance(containerId)
        "TextInput" -> this.textInput = toTextInputInstance(containerId)
        "DragAndDropSortInput" ->
          this.dragAndDropSortInput = toDragAndDropSortInputInstance(containerId)
        "ImageClickInput" -> this.imageClickInput = toImageClickInputInstance(containerId)
        "RatioExpressionInput" ->
          this.ratioExpressionInput = toRatioExpressionInputInstance(containerId)
        "NumericExpressionInput" ->
          this.numericExpressionInput = toNumericExpressionInputInstance(containerId)
        "AlgebraicExpressionInput" ->
          this.algebraicExpressionInput = toAlgebraicExpressionInputInstance(containerId)
        "MathEquationInput" -> this.mathEquationInput = toMathEquationInputInstance(containerId)
        "EndExploration" -> this.endExploration = EndExplorationInstanceDto.getDefaultInstance()
        else -> error("Unsupported interaction ID: $id.")
      }
    }.build()
  }

  private fun GaeInteractionInstance.toContinueInstance(
    containerId: LocalizationTracker.ContainerId
  ): ContinueInstanceDto {
    val builder = ContinueInstanceDto.newBuilder()
    // Only set customization arguments if the placeholder is overwritten, otherwise local
    // 'Continue' text is set in a way where it can be properly localized.
    this.customizationArgs.getSubtitledText(containerId, "buttonText")?.let { buttonText ->
      builder.customizationArgs = ContinueInstanceDto.CustomizationArgsDto.newBuilder().apply {
        this.buttonText = buttonText
      }.build()
    }
    this.defaultOutcome?.let { builder.defaultOutcome = it.toProto(containerId) }
    return builder.build()
  }

  private fun GaeInteractionInstance.toFractionInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): FractionInputInstanceDto {
    return FractionInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toFractionInputInstance.customizationArgs
      this.customizationArgs = FractionInputInstanceDto.CustomizationArgsDto.newBuilder().apply {
        requiresSimplestForm = interactionArgs.getBoolean("requireSimplestForm") ?: false
        allowImproperFractions = interactionArgs.getBoolean("allowImproperFraction") ?: true
        allowNonzeroIntegerPart = interactionArgs.getBoolean("allowNonzeroIntegerPart") ?: true
        placeholder = interactionArgs.getSubtitledTextOrDefault(containerId, "customPlaceholder")
      }.build()

      this.addAllAnswerGroups(
        this@toFractionInputInstance.answerGroups.toFractionAnswerGroups(containerId)
      )
      this@toFractionInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }

      this.addAllHints(this@toFractionInputInstance.hints.toProto(containerId))
      this@toFractionInputInstance.solution?.let {
        this.solution = it.toProto(containerId, FRACTION_INPUT).fractionInstanceSolution
      }
    }.build()
  }

  private fun GaeInteractionInstance.toItemSelectionInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): ItemSelectionInputInstanceDto {
    return ItemSelectionInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toItemSelectionInputInstance.customizationArgs
      this.customizationArgs =
        ItemSelectionInputInstanceDto.CustomizationArgsDto.newBuilder().apply {
          minAllowableSelectionCount = interactionArgs.getInt("minAllowableSelectionCount") ?: 1
          maxAllowableSelectionCount = interactionArgs.getInt("maxAllowableSelectionCount") ?: 1
          addAllChoices(interactionArgs.getSubtitledTextListOrDefault(containerId, "choices"))
        }.build()

      this.addAllAnswerGroups(
        this@toItemSelectionInputInstance.answerGroups.toItemSelectionAnswerGroups(containerId)
      )
      this@toItemSelectionInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }

      this.addAllHints(this@toItemSelectionInputInstance.hints.toProto(containerId))
    }.build()
  }

  private fun GaeInteractionInstance.toMultipleChoiceInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): MultipleChoiceInputInstanceDto {
    return MultipleChoiceInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toMultipleChoiceInputInstance.customizationArgs
      this.customizationArgs =
        MultipleChoiceInputInstanceDto.CustomizationArgsDto.newBuilder().apply {
          addAllChoices(interactionArgs.getSubtitledTextListOrDefault(containerId, "choices"))
        }.build()
      this.addAllAnswerGroups(
        this@toMultipleChoiceInputInstance.answerGroups.toMultipleChoiceAnswerGroups(containerId)
      )
      this@toMultipleChoiceInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }

      this.addAllHints(this@toMultipleChoiceInputInstance.hints.toProto(containerId))
    }.build()
  }

  private fun GaeInteractionInstance.toNumericInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): NumericInputInstanceDto {
    return NumericInputInstanceDto.newBuilder().apply {
      this.addAllAnswerGroups(
        this@toNumericInputInstance.answerGroups.toNumericInputAnswerGroups(containerId)
      )
      this@toNumericInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }
      this.addAllHints(this@toNumericInputInstance.hints.toProto(containerId))
      this@toNumericInputInstance.solution?.let {
        this.solution = it.toProto(containerId, NUMERIC_INPUT).numericInputInstanceSolution
      }
    }.build()
  }

  private fun GaeInteractionInstance.toTextInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): TextInputInstanceDto {
    return TextInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toTextInputInstance.customizationArgs
      this.customizationArgs = TextInputInstanceDto.CustomizationArgsDto.newBuilder().apply {
        placeholder = interactionArgs.getSubtitledTextOrDefault(containerId, "placeholder")
        rows = interactionArgs.getInt("rows") ?: 1
      }.build()

      this.addAllAnswerGroups(
        this@toTextInputInstance.answerGroups.toTextInputAnswerGroups(containerId)
      )
      this@toTextInputInstance.defaultOutcome?.let { this.defaultOutcome = it.toProto(containerId) }

      this.addAllHints(this@toTextInputInstance.hints.toProto(containerId))
      this@toTextInputInstance.solution?.let {
        this.solution = it.toProto(containerId, TEXT_INPUT).textInputInstanceSolution
      }
    }.build()
  }

  private fun GaeInteractionInstance.toDragAndDropSortInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): DragAndDropSortInputInstanceDto {
    return DragAndDropSortInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toDragAndDropSortInputInstance.customizationArgs
      this.customizationArgs =
        DragAndDropSortInputInstanceDto.CustomizationArgsDto.newBuilder().apply {
          addAllChoices(interactionArgs.getSubtitledTextListOrDefault(containerId, "choices"))
          allowMultipleItemsInSamePosition =
            interactionArgs.getBoolean("allowMultipleItemsInSamePosition") ?: false
        }.build()

      this.addAllAnswerGroups(
        this@toDragAndDropSortInputInstance.answerGroups.toDragAndDropAnswerGroups(containerId)
      )
      this@toDragAndDropSortInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }

      this.addAllHints(this@toDragAndDropSortInputInstance.hints.toProto(containerId))
      this@toDragAndDropSortInputInstance.solution?.let {
        this.solution =
          it.toProto(containerId, DRAG_AND_DROP_SORT_INPUT).dragAndDropSortInputInstanceSolution
      }
    }.build()
  }

  private fun GaeInteractionInstance.toImageClickInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): ImageClickInputInstanceDto {
    return ImageClickInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toImageClickInputInstance.customizationArgs
      this.customizationArgs = ImageClickInputInstanceDto.CustomizationArgsDto.newBuilder().apply {
        imageAndRegions = checkNotNull(interactionArgs.getImageWithRegions("imageAndRegions")) {
          "Expected imageAndRegions customization argument to be defined in interaction: $this"
        }
      }.build()
      this.addAllAnswerGroups(
        this@toImageClickInputInstance.answerGroups.toImageClickAnswerGroups(containerId)
      )
      this@toImageClickInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }
      this.addAllHints(this@toImageClickInputInstance.hints.toProto(containerId))
    }.build()
  }

  private fun GaeInteractionInstance.toRatioExpressionInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): RatioExpressionInputInstanceDto {
    return RatioExpressionInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toRatioExpressionInputInstance.customizationArgs
      this.customizationArgs =
        RatioExpressionInputInstanceDto.CustomizationArgsDto.newBuilder().apply {
          placeholder = interactionArgs.getSubtitledTextOrDefault(containerId, "placeholder")
          numberOfTerms = interactionArgs.getInt("numberOfTerms") ?: 0
        }.build()

      this.addAllAnswerGroups(
        this@toRatioExpressionInputInstance.answerGroups.toRatioAnswerGroups(containerId)
      )
      this@toRatioExpressionInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }

      this.addAllHints(this@toRatioExpressionInputInstance.hints.toProto(containerId))
      this@toRatioExpressionInputInstance.solution?.let {
        this.solution =
          it.toProto(containerId, RATIO_EXPRESSION_INPUT).ratioExpressionInputInstanceSolution
      }
    }.build()
  }

  private fun GaeInteractionInstance.toNumericExpressionInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): NumericExpressionInputInstanceDto {
    return NumericExpressionInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toNumericExpressionInputInstance.customizationArgs
      this.customizationArgs =
        NumericExpressionInputInstanceDto.CustomizationArgsDto.newBuilder().apply {
          placeholder = interactionArgs.getSubtitledTextOrDefault(containerId, "placeholder")
          useFractionForDivision = interactionArgs.getBoolean("useFractionForDivision") ?: false
        }.build()

      this.addAllAnswerGroups(
        this@toNumericExpressionInputInstance.answerGroups.toNumericExpressionGroups(containerId)
      )
      this@toNumericExpressionInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }

      this.addAllHints(this@toNumericExpressionInputInstance.hints.toProto(containerId))
      this@toNumericExpressionInputInstance.solution?.let {
        this.solution =
          it.toProto(containerId, NUMERIC_EXPRESSION_INPUT).numericExpressionInputInstanceSolution
      }
    }.build()
  }

  private fun GaeInteractionInstance.toAlgebraicExpressionInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): AlgebraicExpressionInputInstanceDto {
    return AlgebraicExpressionInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toAlgebraicExpressionInputInstance.customizationArgs
      this.customizationArgs =
        AlgebraicExpressionInputInstanceDto.CustomizationArgsDto.newBuilder().apply {
          interactionArgs.getStringList("customOskLetters")?.let(this::addAllCustomOskLetters)
          useFractionForDivision = interactionArgs.getBoolean("useFractionForDivision") ?: false
        }.build()

      this.addAllAnswerGroups(
        this@toAlgebraicExpressionInputInstance.answerGroups.toAlgebraicExpressionGroups(
          containerId
        )
      )
      this@toAlgebraicExpressionInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }

      this.addAllHints(this@toAlgebraicExpressionInputInstance.hints.toProto(containerId))
      this@toAlgebraicExpressionInputInstance.solution?.let {
        this.solution =
          it.toProto(
            containerId, ALGEBRAIC_EXPRESSION_INPUT
          ).algebraicExpressionInputInstanceSolution
      }
    }.build()
  }

  private fun GaeInteractionInstance.toMathEquationInputInstance(
    containerId: LocalizationTracker.ContainerId
  ): MathEquationInputInstanceDto {
    return MathEquationInputInstanceDto.newBuilder().apply {
      val interactionArgs = this@toMathEquationInputInstance.customizationArgs
      this.customizationArgs = MathEquationCustomizationArgsDto.newBuilder().apply {
        interactionArgs.getStringList("customOskLetters")?.let(this::addAllCustomOskLetters)
        useFractionForDivision = interactionArgs.getBoolean("useFractionForDivision") ?: false
      }.build()

      this.addAllAnswerGroups(answerGroups.toMathEquationGroups(containerId))
      this@toMathEquationInputInstance.defaultOutcome?.let {
        this.defaultOutcome = it.toProto(containerId)
      }

      this.addAllHints(this@toMathEquationInputInstance.hints.toProto(containerId))
      this@toMathEquationInputInstance.solution?.let {
        this.solution =
          it.toProto(containerId, MATH_EQUATION_INPUT).mathEquationInputInstanceSolution
      }
    }.build()
  }

  private fun GaeOutcome.toProto(containerId: LocalizationTracker.ContainerId): OutcomeDto {
    return OutcomeDto.newBuilder().apply {
      this@toProto.dest?.let(this::setDestinationState)
      this.feedback = this@toProto.feedback.toProto(containerId)
      this.labelledAsCorrect = this@toProto.labelledAsCorrect
    }.build()
  }

  private fun List<GaeAnswerGroup>.toFractionAnswerGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<FractionInputInstanceDto.AnswerGroupDto> {
    return map { it.toProto(containerId, FRACTION_INPUT).fractionInputInstanceAnswerGroup }
  }

  private fun List<GaeAnswerGroup>.toItemSelectionAnswerGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<ItemSelectionInputInstanceDto.AnswerGroupDto> {
    return map {
      it.toProto(containerId, ITEM_SELECTION_INPUT).itemSelectionInputInstanceAnswerGroup
    }
  }

  private fun List<GaeAnswerGroup>.toMultipleChoiceAnswerGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<MultipleChoiceInputInstanceDto.AnswerGroupDto> {
    return map {
      it.toProto(containerId, MULTIPLE_CHOICE_INPUT).multipleChoiceInputInstanceAnswerGroup
    }
  }

  private fun List<GaeAnswerGroup>.toNumericInputAnswerGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<NumericInputInstanceDto.AnswerGroupDto> {
    return map { it.toProto(containerId, NUMERIC_INPUT).numericInputInstanceAnswerGroup }
  }

  private fun List<GaeAnswerGroup>.toTextInputAnswerGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<TextInputInstanceDto.AnswerGroupDto> {
    return map { it.toProto(containerId, TEXT_INPUT).textInputInstanceAnswerGroup }
  }

  private fun List<GaeAnswerGroup>.toDragAndDropAnswerGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<DragAndDropSortInputInstanceDto.AnswerGroupDto> {
    return map {
      it.toProto(containerId, DRAG_AND_DROP_SORT_INPUT).dragAndDropSortInputInstanceAnswerGroup
    }
  }

  private fun List<GaeAnswerGroup>.toImageClickAnswerGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<ImageClickInputInstanceDto.AnswerGroupDto> {
    return map { it.toProto(containerId, IMAGE_CLICK_INPUT).imageClickInputInstanceAnswerGroup }
  }

  private fun List<GaeAnswerGroup>.toRatioAnswerGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<RatioExpressionInputInstanceDto.AnswerGroupDto> {
    return map {
      it.toProto(containerId, RATIO_EXPRESSION_INPUT).ratioExpressionInputInstanceAnswerGroup
    }
  }

  private fun List<GaeAnswerGroup>.toNumericExpressionGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<NumericExpressionInputInstanceDto.AnswerGroupDto> {
    return map {
      it.toProto(containerId, NUMERIC_EXPRESSION_INPUT).numericExpressionInputInstanceAnswerGroup
    }
  }

  private fun List<GaeAnswerGroup>.toAlgebraicExpressionGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<AlgebraicExpressionInputInstanceDto.AnswerGroupDto> {
    return map {
      it.toProto(
        containerId, ALGEBRAIC_EXPRESSION_INPUT
      ).algebraicExpressionInputInstanceAnswerGroup
    }
  }

  private fun List<GaeAnswerGroup>.toMathEquationGroups(
    containerId: LocalizationTracker.ContainerId
  ): List<MathEquationInputInstanceDto.AnswerGroupDto> {
    return map { it.toProto(containerId, MATH_EQUATION_INPUT).mathEquationInputInstanceAnswerGroup }
  }

  // TODO: Simplify this & the other similar toProto() functions.
  private fun GaeAnswerGroup.toProto(
    containerId: LocalizationTracker.ContainerId,
    interactionType: InteractionTypeCase
  ): AnswerGroup {
    return AnswerGroup.newBuilder().apply {
      when (interactionType) {
        FRACTION_INPUT -> this.fractionInputInstanceAnswerGroup = toFractionAnswerGroup(containerId)
        ITEM_SELECTION_INPUT ->
          this.itemSelectionInputInstanceAnswerGroup = toItemSelectionAnswerGroup(containerId)
        MULTIPLE_CHOICE_INPUT ->
          this.multipleChoiceInputInstanceAnswerGroup = toMultipleChoiceAnswerGroup(containerId)
        NUMERIC_INPUT ->
          this.numericInputInstanceAnswerGroup = toNumericInputAnswerGroup(containerId)
        TEXT_INPUT -> this.textInputInstanceAnswerGroup = toTextInputAnswerGroup(containerId)
        DRAG_AND_DROP_SORT_INPUT ->
          this.dragAndDropSortInputInstanceAnswerGroup = toDragAndDropAnswerGroup(containerId)
        IMAGE_CLICK_INPUT ->
          this.imageClickInputInstanceAnswerGroup = toImageClickInputAnswerGroup(containerId)
        RATIO_EXPRESSION_INPUT ->
          this.ratioExpressionInputInstanceAnswerGroup = toRatioExpressionAnswerGroup(containerId)
        NUMERIC_EXPRESSION_INPUT -> {
          this.numericExpressionInputInstanceAnswerGroup =
            toNumericExpressionAnswerGroup(containerId)
        }
        ALGEBRAIC_EXPRESSION_INPUT -> {
          this.algebraicExpressionInputInstanceAnswerGroup =
            toAlgebraicExpressionAnswerGroup(containerId)
        }
        MATH_EQUATION_INPUT ->
          this.mathEquationInputInstanceAnswerGroup = toMathEquationAnswerGroup(containerId)
        CONTINUE_INSTANCE, END_EXPLORATION, INTERACTIONTYPE_NOT_SET ->
          error("Interaction does not support answer groups: $interactionType.")
      }
    }.build()
  }

  private fun GaeAnswerGroup.toFractionAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): FractionInputInstanceDto.AnswerGroupDto {
    return FractionInputInstanceDto.AnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(this@toFractionAnswerGroup.ruleSpecs.toFractionProtos(containerId))
    }.build()
  }

  private fun GaeAnswerGroup.toItemSelectionAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): ItemSelectionAnswerGroupDto {
    return ItemSelectionAnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(
        this@toItemSelectionAnswerGroup.ruleSpecs.toItemSelectionProtos(containerId)
      )
    }.build()
  }

  private fun GaeAnswerGroup.toMultipleChoiceAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): MultipleChoiceAnswerGroupDto {
    return MultipleChoiceAnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(
        this@toMultipleChoiceAnswerGroup.ruleSpecs.toMultipleChoiceProtos(containerId)
      )
    }.build()
  }

  private fun GaeAnswerGroup.toNumericInputAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): NumericInputInstanceDto.AnswerGroupDto {
    return NumericInputInstanceDto.AnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(
        this@toNumericInputAnswerGroup.ruleSpecs.toNumericInputProtos(containerId)
      )
    }.build()
  }

  private fun GaeAnswerGroup.toTextInputAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): TextInputInstanceDto.AnswerGroupDto {
    return TextInputInstanceDto.AnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(this@toTextInputAnswerGroup.ruleSpecs.toTextInputProtos(containerId))
    }.build()
  }

  private fun GaeAnswerGroup.toDragAndDropAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): DragAndDropAnswerGroupDto {
    return DragAndDropAnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(this@toDragAndDropAnswerGroup.ruleSpecs.toDragAndDropProtos(containerId))
    }.build()
  }

  private fun GaeAnswerGroup.toImageClickInputAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): ImageClickAnswerGroupDto {
    return ImageClickAnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(
        this@toImageClickInputAnswerGroup.ruleSpecs.toImageClickProtos(containerId)
      )
    }.build()
  }

  private fun GaeAnswerGroup.toRatioExpressionAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): RatioExpressionAnswerGroupDto {
    return RatioExpressionAnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(
        this@toRatioExpressionAnswerGroup.ruleSpecs.toRatioExpressionProtos(containerId)
      )
    }.build()
  }

  private fun GaeAnswerGroup.toNumericExpressionAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): NumericExpressionAnswerGroupDto {
    return NumericExpressionAnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(
        this@toNumericExpressionAnswerGroup.ruleSpecs.toNumericExpressionProtos(containerId)
      )
    }.build()
  }

  private fun GaeAnswerGroup.toAlgebraicExpressionAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): AlgebraicExpAnswerGroupDto {
    return AlgebraicExpAnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(
        this@toAlgebraicExpressionAnswerGroup.ruleSpecs.toAlgebraicExpressionProtos(containerId)
      )
    }.build()
  }

  private fun GaeAnswerGroup.toMathEquationAnswerGroup(
    containerId: LocalizationTracker.ContainerId
  ): MathEquationAnswerGroupDto {
    return MathEquationAnswerGroupDto.newBuilder().apply {
      this.baseAnswerGroup = toBaseProto(containerId)
      this.addAllRuleSpecs(
        this@toMathEquationAnswerGroup.ruleSpecs.toMathEquationProtos(containerId)
      )
    }.build()
  }

  private fun GaeAnswerGroup.toBaseProto(
    containerId: LocalizationTracker.ContainerId
  ): BaseAnswerGroupDto {
    return BaseAnswerGroupDto.newBuilder().apply {
      this.outcome = this@toBaseProto.outcome.toProto(containerId)
    }.build()
  }

  private fun List<GaeRuleSpec>.toFractionProtos(containerId: LocalizationTracker.ContainerId) =
    toProtos(FRACTION_INPUT, containerId).map { it.fractionInputInstanceRuleSpec }

  private fun List<GaeRuleSpec>.toItemSelectionProtos(
    containerId: LocalizationTracker.ContainerId
  ): List<ItemSelectionInputInstanceDto.RuleSpecDto> {
    return toProtos(ITEM_SELECTION_INPUT, containerId).map { it.itemSelectionInputInstanceRuleSpec }
  }

  private fun List<GaeRuleSpec>.toMultipleChoiceProtos(
    containerId: LocalizationTracker.ContainerId
  ): List<MultipleChoiceInputInstanceDto.RuleSpecDto> {
    return toProtos(MULTIPLE_CHOICE_INPUT, containerId).map {
      it.multipleChoiceInputInstanceRuleSpec
    }
  }

  private fun List<GaeRuleSpec>.toNumericInputProtos(containerId: LocalizationTracker.ContainerId) =
    toProtos(NUMERIC_INPUT, containerId).map { it.numericInputInstanceRuleSpec }

  private fun List<GaeRuleSpec>.toTextInputProtos(containerId: LocalizationTracker.ContainerId) =
    toProtos(TEXT_INPUT, containerId).map { it.textInputInstanceRuleSpec }

  private fun List<GaeRuleSpec>.toDragAndDropProtos(containerId: LocalizationTracker.ContainerId) =
    toProtos(DRAG_AND_DROP_SORT_INPUT, containerId).map { it.dragAndDropSortInputInstanceRuleSpec }

  private fun List<GaeRuleSpec>.toImageClickProtos(containerId: LocalizationTracker.ContainerId) =
    toProtos(IMAGE_CLICK_INPUT, containerId).map { it.imageClickInputInstanceRuleSpec }

  private fun List<GaeRuleSpec>.toRatioExpressionProtos(
    containerId: LocalizationTracker.ContainerId
  ): List<RatioExpressionInputInstanceDto.RuleSpecDto> {
    return toProtos(RATIO_EXPRESSION_INPUT, containerId).map {
      it.ratioExpressionInputInstanceRuleSpec
    }
  }

  private fun List<GaeRuleSpec>.toNumericExpressionProtos(
    containerId: LocalizationTracker.ContainerId
  ): List<NumericExpressionInputInstanceDto.RuleSpecDto> {
    return toProtos(NUMERIC_EXPRESSION_INPUT, containerId).map {
      it.numericExpressionInputInstanceRuleSpec
    }
  }

  private fun List<GaeRuleSpec>.toAlgebraicExpressionProtos(
    containerId: LocalizationTracker.ContainerId
  ): List<AlgebraicExpressionInputInstanceDto.RuleSpecDto> {
    return toProtos(ALGEBRAIC_EXPRESSION_INPUT, containerId).map {
      it.algebraicExpressionInputInstanceRuleSpec
    }
  }

  private fun List<GaeRuleSpec>.toMathEquationProtos(containerId: LocalizationTracker.ContainerId) =
    toProtos(MATH_EQUATION_INPUT, containerId).map { it.mathEquationInputInstanceRuleSpec }

  private fun List<GaeRuleSpec>.toProtos(
    interactionType: InteractionTypeCase,
    containerId: LocalizationTracker.ContainerId
  ): List<RuleSpec> {
    return map { it.toProto(interactionType, containerId) }
  }

  private fun GaeRuleSpec.toProto(
    interType: InteractionTypeCase,
    containerId: LocalizationTracker.ContainerId
  ): RuleSpec {
    return RuleSpec.newBuilder().apply {
      when (interType) {
        FRACTION_INPUT -> this.fractionInputInstanceRuleSpec = toFractionRuleSpec(containerId)
        ITEM_SELECTION_INPUT ->
          this.itemSelectionInputInstanceRuleSpec = toItemSelectionRuleSpec(containerId)
        MULTIPLE_CHOICE_INPUT ->
          this.multipleChoiceInputInstanceRuleSpec = toMultipleChoiceRuleSpec(containerId)
        NUMERIC_INPUT -> this.numericInputInstanceRuleSpec = toNumericInputRuleSpec(containerId)
        TEXT_INPUT -> this.textInputInstanceRuleSpec = toTextInputRuleSpec(containerId)
        DRAG_AND_DROP_SORT_INPUT ->
          this.dragAndDropSortInputInstanceRuleSpec = toDragAndDropRuleSpec(containerId)
        IMAGE_CLICK_INPUT ->
          this.imageClickInputInstanceRuleSpec = toImageClickRuleSpec(containerId)
        RATIO_EXPRESSION_INPUT ->
          this.ratioExpressionInputInstanceRuleSpec = toRatioExpressionRuleSpec(containerId)
        NUMERIC_EXPRESSION_INPUT ->
          this.numericExpressionInputInstanceRuleSpec = toNumericExpressionRuleSpec(containerId)
        ALGEBRAIC_EXPRESSION_INPUT ->
          this.algebraicExpressionInputInstanceRuleSpec = toAlgebraicExpressionRuleSpec(containerId)
        MATH_EQUATION_INPUT ->
          this.mathEquationInputInstanceRuleSpec = toMathEquationRuleSpec(containerId)
        CONTINUE_INSTANCE, END_EXPLORATION, INTERACTIONTYPE_NOT_SET ->
          error("Interaction does not support rule specs: $interType.")
      }
    }.build()
  }

  private fun GaeRuleSpec.toFractionRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): FractionInputInstanceDto.RuleSpecDto {
    return FractionInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toFractionRuleSpec.inputs
      when (val ruleType = this@toFractionRuleSpec.ruleType) {
        "IsExactlyEqualTo" -> {
          this.isExactlyEqualTo = FractionIsExactlyEqualToSpec.newBuilder().apply {
            this.input = inputMap.getFractionInput(name = "f", containerId)
          }.build()
        }
        "IsEquivalentTo" -> {
          this.isEquivalentTo = FractionIsEquivalentToSpec.newBuilder().apply {
            this.input = inputMap.getFractionInput(name = "f", containerId)
          }.build()
        }
        "IsEquivalentToAndInSimplestForm" -> {
          this.isEquivalentToAndInSimplestForm =
            FractionIsEquivalentToAndInSimplestFormSpec.newBuilder().apply {
              this.input = inputMap.getFractionInput(name = "f", containerId)
            }.build()
        }
        "IsLessThan" -> {
          this.isLessThan = FractionIsLessThanSpec.newBuilder().apply {
            this.input = inputMap.getFractionInput(name = "f", containerId)
          }.build()
        }
        "IsGreaterThan" -> {
          this.isGreaterThan = FractionIsGreaterThanSpec.newBuilder().apply {
            this.input = inputMap.getFractionInput(name = "f", containerId)
          }.build()
        }
        "HasNumeratorEqualTo" -> {
          this.hasNumeratorEqualTo = FractionHasNumeratorEqualToSpec.newBuilder().apply {
            this.input = inputMap.getIntInput(name = "x", containerId)
          }.build()
        }
        "HasDenominatorEqualTo" -> {
          this.hasDenominatorEqualTo = FractionHasDenominatorEqualToSpec.newBuilder().apply {
            this.input = inputMap.getNonNegativeIntInput(name = "x", containerId)
          }.build()
        }
        "HasIntegerPartEqualTo" -> {
          this.hasIntegerPartEqualTo = FractionHasIntegerPartEqualToSpec.newBuilder().apply {
            this.input = inputMap.getIntInput(name = "x", containerId)
          }.build()
        }
        "HasNoFractionalPart" ->
          this.hasNoFractionalPart = FractionHasNoFractionalPartSpec.getDefaultInstance()
        "HasFractionalPartExactlyEqualTo" -> {
          this.hasFractionalPartExactlyEqualTo =
            FractionHasFractionalPartExactlyEqualToSpec.newBuilder().apply {
              this.input = inputMap.getFractionInput(name = "f", containerId)
            }.build()
        }
        else -> error("Unknown rule type: $ruleType.")
      }
    }.build()
  }

  private fun GaeRuleSpec.toItemSelectionRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): ItemSelectionInputInstanceDto.RuleSpecDto {
    return ItemSelectionInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toItemSelectionRuleSpec.inputs
      when (val ruleType = this@toItemSelectionRuleSpec.ruleType) {
        "Equals" -> {
          this.equals = ItemSelectionEqualsSpec.newBuilder().apply {
            this.input = inputMap.getSetOfTranslatableHtmlContentIds(name = "x", containerId)
          }.build()
        }
        "ContainsAtLeastOneOf" -> {
          this.containsAtLeastOneOf = ItemSelectionContainsAtLeastOneOfSpec.newBuilder().apply {
            this.input = inputMap.getSetOfTranslatableHtmlContentIds(name = "x", containerId)
          }.build()
        }
        "DoesNotContainAtLeastOneOf" -> {
          this.doesNotContainAtLeastOneOf =
            ItemSelectionDoesNotContainAtLeastOneOfSpec.newBuilder().apply {
              this.input = inputMap.getSetOfTranslatableHtmlContentIds(name = "x", containerId)
            }.build()
        }
        "IsProperSubsetOf" -> {
          this.isProperSubsetOf = ItemSelectionIsProperSubsetOfSpec.newBuilder().apply {
            this.input = inputMap.getSetOfTranslatableHtmlContentIds(name = "x", containerId)
          }.build()
        }
        else -> error("Unknown rule type: $ruleType.")
      }
    }.build()
  }

  private fun GaeRuleSpec.toMultipleChoiceRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): MultipleChoiceInputInstanceDto.RuleSpecDto {
    return MultipleChoiceInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toMultipleChoiceRuleSpec.inputs
      when (val ruleType = this@toMultipleChoiceRuleSpec.ruleType) {
        "Equals" -> {
          this.equals = MultipleChoiceEqualsSpec.newBuilder().apply {
            this.input = inputMap.getNonNegativeIntInput(name = "x", containerId)
          }.build()
        }
        else -> error("Unknown rule type: $ruleType.")
      }
    }.build()
  }

  private fun GaeRuleSpec.toNumericInputRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): NumericInputInstanceDto.RuleSpecDto {
    return NumericInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toNumericInputRuleSpec.inputs
      when (val ruleType = this@toNumericInputRuleSpec.ruleType) {
        "Equals" -> {
          this.equals = NumericEqualsSpec.newBuilder().apply {
            this.input = inputMap.getRealInput(name = "x", containerId)
          }.build()
        }
        "IsLessThan" -> {
          this.isLessThan = NumericIsLessThanSpec.newBuilder().apply {
            this.input = inputMap.getRealInput(name = "x", containerId)
          }.build()
        }
        "IsGreaterThan" -> {
          this.isGreaterThan = NumericIsGreaterThanSpec.newBuilder().apply {
            this.input = inputMap.getRealInput(name = "x", containerId)
          }.build()
        }
        "IsLessThanOrEqualTo" -> {
          this.isLessThanOrEqualTo = NumericIsLessThanOrEqualToSpec.newBuilder().apply {
            this.input = inputMap.getRealInput(name = "x", containerId)
          }.build()
        }
        "IsGreaterThanOrEqualTo" -> {
          this.isGreaterThanOrEqualTo = NumericIsGreaterThanOrEqualToSpec.newBuilder().apply {
            this.input = inputMap.getRealInput(name = "x", containerId)
          }.build()
        }
        "IsInclusivelyBetween" -> {
          this.isInclusivelyBetween = NumericIsInclusivelyBetweenSpec.newBuilder().apply {
            this.inputLowerInclusive = inputMap.getRealInput(name = "a", containerId)
            this.inputUpperInclusive = inputMap.getRealInput(name = "b", containerId)
          }.build()
        }
        "IsWithinTolerance" -> {
          this.isWithinTolerance = NumericIsWithinToleranceSpec.newBuilder().apply {
            this.inputTolerance = inputMap.getRealInput(name = "tol", containerId)
            this.inputComparedValue = inputMap.getRealInput(name = "x", containerId)
          }.build()
        }
        else -> error("Unknown rule type: $ruleType.")
      }
    }.build()
  }

  private fun GaeRuleSpec.toTextInputRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): TextInputInstanceDto.RuleSpecDto {
    return TextInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toTextInputRuleSpec.inputs
      when (val ruleType = this@toTextInputRuleSpec.ruleType) {
        "Equals" -> {
          this.equals = TextEqualsSpec.newBuilder().apply {
            this.input = inputMap.getTranslatableSetOfNormalizedString(name = "x", containerId)
          }.build()
        }
        "StartsWith" -> {
          this.startsWith = TextStartsWithSpec.newBuilder().apply {
            this.input = inputMap.getTranslatableSetOfNormalizedString(name = "x", containerId)
          }.build()
        }
        "Contains" -> {
          this.contains = TextContainsSpec.newBuilder().apply {
            this.input = inputMap.getTranslatableSetOfNormalizedString(name = "x", containerId)
          }.build()
        }
        "FuzzyEquals" -> {
          this.fuzzyEquals = TextFuzzyEqualsSpec.newBuilder().apply {
            this.input = inputMap.getTranslatableSetOfNormalizedString(name = "x", containerId)
          }.build()
        }
        else -> error("Unknown rule type: $ruleType.")
      }
    }.build()
  }

  private fun GaeRuleSpec.toDragAndDropRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): DragAndDropSortInputInstanceDto.RuleSpecDto {
    return DragAndDropSortInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toDragAndDropRuleSpec.inputs
      when (val ruleType = this@toDragAndDropRuleSpec.ruleType) {
        "IsEqualToOrdering" -> {
          this.isEqualToOrdering = DragAndDropIsEqualToOrderingSpec.newBuilder().apply {
            this.input = inputMap.getListOfSetsOfTranslatableHtmlContentIds(name = "x", containerId)
          }.build()
        }
        "IsEqualToOrderingWithOneItemAtIncorrectPosition" -> {
          this.isEqualToOrderingWithOneItemAtIncorrectPosition =
            DragAndDropIsEqualToOrderingWithOneItemAtIncorrectPositionSpec.newBuilder().apply {
              this.input =
                inputMap.getListOfSetsOfTranslatableHtmlContentIds(name = "x", containerId)
            }.build()
        }
        "HasElementXAtPositionY" -> {
          this.hasElementXAtPositionY = DragAndDropHasElementXAtPositionYSpec.newBuilder().apply {
            this.element = inputMap.getTranslatableHtmlContentId(name = "x", containerId)
            this.position = inputMap.getNonNegativeIntInput(name = "y", containerId)
          }.build()
        }
        "HasElementXBeforeElementY" -> {
          this.hasElementXBeforeElementY =
            DragAndDropHasElementXBeforeElementYSpec.newBuilder().apply {
              this.consideredElement =
                inputMap.getTranslatableHtmlContentId(name = "x", containerId)
              this.laterElement = inputMap.getTranslatableHtmlContentId(name = "y", containerId)
            }.build()
        }
        else -> error("Unknown rule type: $ruleType.")
      }
    }.build()
  }

  private fun GaeRuleSpec.toImageClickRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): ImageClickInputInstanceDto.RuleSpecDto {
    return ImageClickInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toImageClickRuleSpec.inputs
      when (val ruleType = this@toImageClickRuleSpec.ruleType) {
        "IsInRegion" -> {
          this.isInRegion = ImageClickIsInRegionSpec.newBuilder().apply {
            this.inputRegion = inputMap.getNormalizedStringInput(name = "x", containerId)
          }.build()
        }
        else -> error("Unknown rule type: $ruleType.")
      }
    }.build()
  }

  private fun GaeRuleSpec.toRatioExpressionRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): RatioExpressionInputInstanceDto.RuleSpecDto {
    return RatioExpressionInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toRatioExpressionRuleSpec.inputs
      when (val ruleType = this@toRatioExpressionRuleSpec.ruleType) {
        "Equals" -> {
          this.equals = RatioEqualsSpec.newBuilder().apply {
            this.input = inputMap.getRatioExpression(name = "x", containerId)
          }.build()
        }
        "IsEquivalent" -> {
          this.isEquivalent = RatioIsEquivalentSpec.newBuilder().apply {
            this.input = inputMap.getRatioExpression(name = "x", containerId)
          }.build()
        }
        "HasNumberOfTermsEqualTo" -> {
          this.hasNumberOfTermsEqualTo = RatioHasNumberOfTermsEqualToSpec.newBuilder().apply {
            this.inputTermCount = inputMap.getNonNegativeIntInput(name = "y", containerId)
          }.build()
        }
        "HasSpecificTermEqualTo" -> {
          this.hasSpecificTermEqualTo = RatioHasSpecificTermEqualToSpec.newBuilder().apply {
            this.inputTermIndex = inputMap.getNonNegativeIntInput(name = "x", containerId)
            this.inputExpectedTermValue = inputMap.getNonNegativeIntInput(name = "y", containerId)
          }.build()
        }
        else -> error("Unknown rule type: $ruleType.")
      }
    }.build()
  }

  private fun GaeRuleSpec.toNumericExpressionRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): NumericExpressionInputInstanceDto.RuleSpecDto {
    return NumericExpressionInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toNumericExpressionRuleSpec.inputs
      when (val ruleType = this@toNumericExpressionRuleSpec.ruleType) {
        "MatchesExactlyWith" -> {
          this.matchesExactlyWith = NumericExpressionMatchesExactlySpec.newBuilder().apply {
            this.numericExpression = inputMap.getMathExpression(name = "x", containerId)
          }.build()
        }
        "MatchesUpToTrivialManipulations" -> {
          this.matchesUpToTrivialManipulations =
            NumericExpressionTrivialManipsSpec.newBuilder().apply {
              this.numericExpression = inputMap.getMathExpression(name = "x", containerId)
            }.build()
        }
        "IsEquivalentTo" -> {
          this.isEquivalentTo = NumericExpressionIsEquivalentSpec.newBuilder().apply {
            this.numericExpression = inputMap.getMathExpression(name = "x", containerId)
          }.build()
        }
        else -> error("Unknown rule type: $ruleType")
      }
    }.build()
  }

  private fun GaeRuleSpec.toAlgebraicExpressionRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): AlgebraicExpressionInputInstanceDto.RuleSpecDto {
    return AlgebraicExpressionInputInstanceDto.RuleSpecDto.newBuilder().apply {
      val inputMap = this@toAlgebraicExpressionRuleSpec.inputs
      when (val ruleType = this@toAlgebraicExpressionRuleSpec.ruleType) {
        "MatchesExactlyWith" -> {
          this.matchesExactlyWith = AlgebraicExpressionMatchesExactlySpec.newBuilder().apply {
            this.algebraicExpression = inputMap.getMathExpression(name = "x", containerId)
          }.build()
        }
        "MatchesUpToTrivialManipulations" -> {
          this.matchesUpToTrivialManipulations =
            AlgebraicExpressionTrivialManipsSpec.newBuilder().apply {
              this.algebraicExpression = inputMap.getMathExpression(name = "x", containerId)
            }.build()
        }
        "IsEquivalentTo" -> {
          this.isEquivalentTo = AlgebraicExpressionIsEquivalentSpec.newBuilder().apply {
            this.algebraicExpression = inputMap.getMathExpression(name = "x", containerId)
          }.build()
        }
        else -> error("Unknown rule type: $ruleType")
      }
    }.build()
  }

  private fun GaeRuleSpec.toMathEquationRuleSpec(
    containerId: LocalizationTracker.ContainerId
  ): MathEquationInputInstanceDto.RuleSpecDto {
    return MathEquationRuleSpecDto.newBuilder().apply {
      val inputMap = this@toMathEquationRuleSpec.inputs
      when (val ruleType = this@toMathEquationRuleSpec.ruleType) {
        "MatchesExactlyWith" -> {
          this.matchesExactlyWith = MathEquationMatchesExactlySpec.newBuilder().apply {
            this.mathEquation = inputMap.getMathExpression(name = "x", containerId)
          }.build()
        }
        "MatchesUpToTrivialManipulations" -> {
          this.matchesUpToTrivialManipulations = MathEquationTrivialManipsSpec.newBuilder().apply {
            this.mathEquation = inputMap.getMathExpression(name = "x", containerId)
          }.build()
        }
        "IsEquivalentTo" -> {
          this.isEquivalentTo = MathEquationIsEquivalentSpec.newBuilder().apply {
            this.mathEquation = inputMap.getMathExpression(name = "x", containerId)
          }.build()
        }
        else -> error("Unknown rule type: $ruleType")
      }
    }.build()
  }

  private fun List<GaeHint>.toProto(containerId: LocalizationTracker.ContainerId) =
    map { it.toProto(containerId) }

  private fun GaeHint.toProto(containerId: LocalizationTracker.ContainerId): HintDto {
    return HintDto.newBuilder().apply {
      this.hintContent = this@toProto.hintContent.toProto(containerId)
    }.build()
  }

  private fun GaeSolution.toProto(
    containerId: LocalizationTracker.ContainerId,
    interType: InteractionTypeCase
  ): Solution {
    return Solution.newBuilder().apply {
      when (interType) {
        FRACTION_INPUT -> this.fractionInstanceSolution = toFractionInputSolution(containerId)
        NUMERIC_INPUT -> this.numericInputInstanceSolution = toNumericInputSolution(containerId)
        TEXT_INPUT -> this.textInputInstanceSolution = toTextInputSolution(containerId)
        DRAG_AND_DROP_SORT_INPUT ->
          this.dragAndDropSortInputInstanceSolution = toDragAndDropSortInputSolution(containerId)
        RATIO_EXPRESSION_INPUT ->
          this.ratioExpressionInputInstanceSolution = toRatioExpressionInputSolution(containerId)
        NUMERIC_EXPRESSION_INPUT -> {
          this.numericExpressionInputInstanceSolution =
            toNumericExpressionInputSolution(containerId)
        }
        ALGEBRAIC_EXPRESSION_INPUT -> {
          this.algebraicExpressionInputInstanceSolution =
            toAlgebraicExpressionInputSolution(containerId)
        }
        MATH_EQUATION_INPUT ->
          this.mathEquationInputInstanceSolution = toMathEquationInputSolution(containerId)
        // Interactions that do not support solutions.
        CONTINUE_INSTANCE, ITEM_SELECTION_INPUT, MULTIPLE_CHOICE_INPUT, IMAGE_CLICK_INPUT,
        END_EXPLORATION, INTERACTIONTYPE_NOT_SET ->
          error("Interaction does not support solutions: $interType.")
      }
    }.build()
  }

  private fun GaeSolution.toFractionInputSolution(
    containerId: LocalizationTracker.ContainerId
  ): FractionInputInstanceDto.SolutionDto {
    return FractionInputInstanceDto.SolutionDto.newBuilder().apply {
      this.baseSolution = toBaseProto(containerId)
      this.correctAnswer =
        this@toFractionInputSolution.correctAnswer.toExpectedUserAnswer(
          expectedType = FRACTION, containerId
        ).fraction
    }.build()
  }

  private fun GaeSolution.toNumericInputSolution(
    containerId: LocalizationTracker.ContainerId
  ): NumericInputInstanceDto.SolutionDto {
    return NumericInputInstanceDto.SolutionDto.newBuilder().apply {
      this.baseSolution = toBaseProto(containerId)
      this.correctAnswer =
        this@toNumericInputSolution.correctAnswer.toExpectedUserAnswer(
          expectedType = REAL, containerId
        ).real
    }.build()
  }

  private fun GaeSolution.toTextInputSolution(
    containerId: LocalizationTracker.ContainerId
  ): TextInputInstanceDto.SolutionDto {
    return TextInputInstanceDto.SolutionDto.newBuilder().apply {
      this.baseSolution = toBaseProto(containerId)
      this.correctAnswer =
        this@toTextInputSolution.correctAnswer.toExpectedUserAnswer(
          expectedType = NORMALIZED_STRING, containerId
        ).normalizedString
    }.build()
  }

  private fun GaeSolution.toDragAndDropSortInputSolution(
    containerId: LocalizationTracker.ContainerId
  ): DragAndDropSortInputInstanceDto.SolutionDto {
    return DragAndDropSortInputInstanceDto.SolutionDto.newBuilder().apply {
      this.baseSolution = toBaseProto(containerId)
      this.correctAnswer = this@toDragAndDropSortInputSolution.correctAnswer.toExpectedUserAnswer(
        expectedType = LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS, containerId
      ).listOfSetsOfTranslatableHtmlContentIds
    }.build()
  }

  private fun GaeSolution.toRatioExpressionInputSolution(
    containerId: LocalizationTracker.ContainerId
  ): RatioExpressionInputInstanceDto.SolutionDto {
    return RatioExpressionInputInstanceDto.SolutionDto.newBuilder().apply {
      this.baseSolution = toBaseProto(containerId)
      this.correctAnswer =
        this@toRatioExpressionInputSolution.correctAnswer.toExpectedUserAnswer(
          expectedType = RATIO_EXPRESSION, containerId
        ).ratioExpression
    }.build()
  }

  private fun GaeSolution.toNumericExpressionInputSolution(
    containerId: LocalizationTracker.ContainerId
  ): NumericExpressionInputInstanceDto.SolutionDto {
    return NumericExpressionInputInstanceDto.SolutionDto.newBuilder().apply {
      this.baseSolution = toBaseProto(containerId)
      this.correctAnswer =
        this@toNumericExpressionInputSolution.correctAnswer.toExpectedUserAnswer(
          expectedType = MATH_EXPRESSION, containerId
        ).mathExpression
    }.build()
  }

  private fun GaeSolution.toAlgebraicExpressionInputSolution(
    containerId: LocalizationTracker.ContainerId
  ): AlgebraicExpressionInputInstanceDto.SolutionDto {
    return AlgebraicExpressionInputInstanceDto.SolutionDto.newBuilder().apply {
      this.baseSolution = toBaseProto(containerId)
      this.correctAnswer =
        this@toAlgebraicExpressionInputSolution.correctAnswer.toExpectedUserAnswer(
          expectedType = MATH_EXPRESSION, containerId
        ).mathExpression
    }.build()
  }

  private fun GaeSolution.toMathEquationInputSolution(
    containerId: LocalizationTracker.ContainerId
  ): MathEquationInputInstanceDto.SolutionDto {
    return MathEquationInputInstanceDto.SolutionDto.newBuilder().apply {
      this.baseSolution = toBaseProto(containerId)
      this.correctAnswer =
        this@toMathEquationInputSolution.correctAnswer.toExpectedUserAnswer(
          expectedType = MATH_EXPRESSION, containerId
        ).mathExpression
    }.build()
  }

  private fun GaeSolution.toBaseProto(
    containerId: LocalizationTracker.ContainerId
  ): BaseSolutionDto {
    return BaseSolutionDto.newBuilder().apply {
      this.explanation = this@toBaseProto.explanation.toProto(containerId)
    }.build()
  }

  private fun GaeSubtitledHtml.toProto(containerId: LocalizationTracker.ContainerId) =
    localizationTracker.convertContainerText(containerId, this)

  private fun GaeSubtitledUnicode.toProto(containerId: LocalizationTracker.ContainerId) =
    localizationTracker.convertContainerText(containerId, this)

  private fun GaeImageWithRegions.toProto() = ImageWithRegionsDto.newBuilder().apply {
    this.imageFilePath = this@toProto.imagePath
    this.addAllLabeledRegions(this@toProto.labeledRegions.map { it.toProto() })
  }.build()

  private fun GaeLabeledRegion.toProto() = LabeledRegionDto.newBuilder().apply {
    this.label = this@toProto.label
    check(this@toProto.region.regionType == "Rectangle") {
      "Only rectangular regions are supported by the pipeline, encountered:" +
        " ${this@toProto.region.regionType}."
    }
    this.normalizedRectangle2D = this@toProto.region.area.toProto()
  }.build()

  private fun GaeNormalizedRectangle2d.toProto() = NormalizedRectangle2dDto.newBuilder().apply {
    this.topLeft = this@toProto.items[0].toProto()
    this.bottomRight = this@toProto.items[1].toProto()
  }.build()

  private fun List<Double>.toProto() = NormalizedPoint2dDto.newBuilder().apply {
    this.x = this@toProto[0]
    this.y = this@toProto[1]
  }.build()

  private fun GaeInteractionArgsMap.getSubtitledText(
    containerId: LocalizationTracker.ContainerId,
    name: String
  ): SubtitledTextDto? {
    return getArg<GaeCustomizationArgValue.SubtitledUnicode>(name)?.value?.toProto(containerId)
  }

  private fun GaeInteractionArgsMap.getSubtitledTextOrDefault(
    containerId: LocalizationTracker.ContainerId,
    name: String,
    createDefault: () -> SubtitledTextDto = { SubtitledTextDto.getDefaultInstance() }
  ): SubtitledTextDto = getSubtitledText(containerId, name) ?: createDefault()

  private fun GaeInteractionArgsMap.getSubtitledTextListOrDefault(
    containerId: LocalizationTracker.ContainerId,
    name: String,
    createDefault: () -> List<SubtitledTextDto> = { listOf(SubtitledTextDto.getDefaultInstance()) }
  ): List<SubtitledTextDto> {
    return getArg<GaeCustomizationArgValue.SubtitledTextList>(name)?.value?.map {
      it.toProto(containerId)
    } ?: createDefault()
  }

  private fun GaeInteractionArgsMap.getBoolean(name: String): Boolean? =
    getArg<GaeCustomizationArgValue.SingleBoolean>(name)?.value

  private fun GaeInteractionArgsMap.getInt(name: String): Int? =
    getArg<GaeCustomizationArgValue.SingleInteger>(name)?.value

  private fun GaeInteractionArgsMap.getStringList(name: String): List<String>? =
    getArg<GaeCustomizationArgValue.StringList>(name)?.value

  private fun GaeInteractionArgsMap.getImageWithRegions(name: String): ImageWithRegionsDto? =
    getArg<GaeImageWithRegions>(name)?.toProto()

  private inline fun <reified T : GaeCustomizationArgValue> GaeInteractionArgsMap.getArg(
    name: String
  ) = customizationArgs[name] as? T

  private fun Map<String, GaeInteractionObject>.getFractionInput(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): FractionDto = getRuleInput(name, RuleInputTypeCase.FRACTION, containerId).fraction

  private fun Map<String, GaeInteractionObject>.getIntInput(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): Int = getRuleInput(name, RuleInputTypeCase.INT, containerId).int

  private fun Map<String, GaeInteractionObject>.getNonNegativeIntInput(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): Int = getRuleInput(name, RuleInputTypeCase.NON_NEGATIVE_INT, containerId).nonNegativeInt

  private fun Map<String, GaeInteractionObject>.getSetOfTranslatableHtmlContentIds(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): SetOfTranslatableHtmlContentIdsDto {
    return getRuleInput(
      name,
      RuleInputTypeCase.SET_OF_TRANSLATABLE_HTML_CONTENT_IDS,
      containerId
    ).setOfTranslatableHtmlContentIds
  }

  private fun Map<String, GaeInteractionObject>.getRealInput(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): Double = getRuleInput(name, RuleInputTypeCase.REAL, containerId).real

  private fun Map<String, GaeInteractionObject>.getTranslatableSetOfNormalizedString(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): TranslatableSetOfNormalizedStringDto {
    return getRuleInput(
      name,
      RuleInputTypeCase.TRANSLATABLE_SET_OF_NORMALIZED_STRING,
      containerId
    ).translatableSetOfNormalizedString
  }

  private fun Map<String, GaeInteractionObject>.getListOfSetsOfTranslatableHtmlContentIds(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): ListOfSetsOfTranslatableHtmlContentIdsDto {
    return getRuleInput(
      name, RuleInputTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS, containerId
    ).listOfSetsOfTranslatableHtmlContentIds
  }

  private fun Map<String, GaeInteractionObject>.getTranslatableHtmlContentId(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): TranslatableHtmlContentIdDto {
    return getRuleInput(
      name, RuleInputTypeCase.TRANSLATABLE_HTML_CONTENT_ID, containerId
    ).translatableHtmlContentId
  }

  private fun Map<String, GaeInteractionObject>.getNormalizedStringInput(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): String = getRuleInput(name, RuleInputTypeCase.NORMALIZED_STRING, containerId).normalizedString

  private fun Map<String, GaeInteractionObject>.getRatioExpression(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): RatioExpressionDto {
    return getRuleInput(name, RuleInputTypeCase.RATIO_EXPRESSION, containerId).ratioExpression
  }

  private fun Map<String, GaeInteractionObject>.getMathExpression(
    name: String,
    containerId: LocalizationTracker.ContainerId
  ): String = getRuleInput(name, RuleInputTypeCase.MATH_EXPRESSION, containerId).mathExpression

  private fun Map<String, GaeInteractionObject>.getRuleInput(
    name: String,
    expectedType: RuleInputTypeCase,
    containerId: LocalizationTracker.ContainerId
  ): RuleInputType = getValue(name).toExpectedRuleInputType(expectedType, containerId)

  private fun GaeInteractionObject.toExpectedUserAnswer(
    expectedType: SolutionAnswer.AnswerTypeCase,
    containerId: LocalizationTracker.ContainerId
  ): SolutionAnswer {
    return toSolutionAnswerProto(containerId).also {
      // Verify the answer type is actually correct.
      check(it.answerTypeCase == expectedType) {
        "Converted proto does not have expected type ($expectedType): $it."
      }
    }
  }

  private fun GaeInteractionObject.toExpectedRuleInputType(
    expectedType: RuleInputType.InputTypeCase,
    containerId: LocalizationTracker.ContainerId
  ): RuleInputType {
    return toRuleInputTypeProto(containerId).also {
      // Verify the input type is actually correct.
      check(it.inputTypeCase == expectedType) {
        "Converted proto does not have expected type ($expectedType): $it."
      }
    }
  }

  private fun GaeInteractionObject.toSolutionAnswerProto(
    containerId: LocalizationTracker.ContainerId
  ): SolutionAnswer {
    return SolutionAnswer.newBuilder().apply {
      when (this@toSolutionAnswerProto) {
        is Fraction -> this.fraction = toProto()
        is SetsOfXlatableContentIds ->
          this.listOfSetsOfTranslatableHtmlContentIds = toProto(containerId)
        is MathExpression -> this.mathExpression = this@toSolutionAnswerProto.value
        is NonNegativeInt -> this.nonNegativeInt = this@toSolutionAnswerProto.value
        is NormalizedString -> this.normalizedString = this@toSolutionAnswerProto.value
        is RatioExpression -> this.ratioExpression = toProto()
        is Real -> this.real = this@toSolutionAnswerProto.value
        is SetOfXlatableContentIds -> this.setOfTranslatableHtmlContentIds = toProto(containerId)
        is SignedInt, is TranslatableHtmlContentId, is TranslatableSetOfNormalizedString ->
          error("Interaction object is not a supported solution answer: $this.")
      }
    }.build()
  }

  private fun GaeInteractionObject.toRuleInputTypeProto(
    containerId: LocalizationTracker.ContainerId
  ): RuleInputType {
    return RuleInputType.newBuilder().apply {
      when (this@toRuleInputTypeProto) {
        is Fraction -> fraction = toProto()
        is SetsOfXlatableContentIds -> listOfSetsOfTranslatableHtmlContentIds = toProto(containerId)
        is MathExpression -> mathExpression = value
        is NonNegativeInt -> nonNegativeInt = value
        is NormalizedString -> normalizedString = value
        is RatioExpression -> ratioExpression = toProto()
        is Real -> real = value
        is SetOfXlatableContentIds -> setOfTranslatableHtmlContentIds = toProto(containerId)
        is SignedInt -> int = value
        is TranslatableHtmlContentId -> translatableHtmlContentId = toProto(containerId)
        is TranslatableSetOfNormalizedString ->
          translatableSetOfNormalizedString = toProto(containerId)
      }
    }.build()
  }

  private fun Fraction.toProto() = FractionDto.newBuilder().apply {
    this.isNegative = this@toProto.isNegative
    this.wholeNumber = this@toProto.wholeNumber
    this.numerator = this@toProto.numerator
    this.denominator = this@toProto.denominator
  }.build()

  private fun SetsOfXlatableContentIds.toProto(
    containerId: LocalizationTracker.ContainerId
  ): ListOfSetsOfTranslatableHtmlContentIdsDto {
    return ListOfSetsOfTranslatableHtmlContentIdsDto.newBuilder().apply {
      this.addAllContentIdSets(this@toProto.sets.map { it.toProto(containerId) })
    }.build()
  }

  private fun RatioExpression.toProto() = RatioExpressionDto.newBuilder().apply {
    this.addAllComponents(this@toProto.ratioComponents)
  }.build()

  private fun SetOfXlatableContentIds.toProto(
    containerId: LocalizationTracker.ContainerId
  ): SetOfTranslatableHtmlContentIdsDto {
    return SetOfTranslatableHtmlContentIdsDto.newBuilder().apply {
      this.addAllContentIds(this@toProto.contentIds.map { it.toProto(containerId) })
    }.build()
  }

  private fun TranslatableHtmlContentId.toProto(
    containerId: LocalizationTracker.ContainerId
  ): TranslatableHtmlContentIdDto {
    return TranslatableHtmlContentIdDto.newBuilder().apply {
      this.contentId = localizationTracker.verifyContentId(containerId, this@toProto.contentId)
    }.build()
  }

  private fun TranslatableSetOfNormalizedString.toProto(
    containerId: LocalizationTracker.ContainerId
  ): TranslatableSetOfNormalizedStringDto {
    return TranslatableSetOfNormalizedStringDto.newBuilder().apply {
      this@toProto.contentId?.let {
        this.contentId = localizationTracker.verifyContentId(containerId, it)
      }
    }.build()
  }

  private fun computeTopicDependencies(topicId: String): Set<String> {
    // Note that topics may have dependencies that won't be available until those topics are
    // introduced in the app. The app gracefully ignores these.
    return topicDependencies[topicId]
      ?: error("Encountered unknown topic while computing dependencies: $topicId.")
  }
}

package org.oppia.android.scripts.assets

import org.oppia.android.app.model.AnswerGroup
import org.oppia.android.app.model.ChapterRecord
import org.oppia.android.app.model.ConceptCard
import org.oppia.android.app.model.ConceptCardList
import org.oppia.android.app.model.CustomSchemaValue
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.HtmlTranslationList
import org.oppia.android.app.model.ImageWithRegions
import org.oppia.android.app.model.ImageWithRegions.LabeledRegion
import org.oppia.android.app.model.ImageWithRegions.LabeledRegion.Region.NormalizedRectangle2d
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.Misconception
import org.oppia.android.app.model.Outcome
import org.oppia.android.app.model.Point2d
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.model.RuleSpec
import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.model.SchemaObjectList
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.State
import org.oppia.android.app.model.StoryRecord
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.SubtitledUnicode
import org.oppia.android.app.model.SubtopicRecord
import org.oppia.android.app.model.ClassroomList
import org.oppia.android.app.model.TopicRecord
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.TranslatableSetOfNormalizedString
import org.oppia.android.app.model.Translation
import org.oppia.android.app.model.TranslationMapping
import org.oppia.android.app.model.Voiceover
import org.oppia.android.app.model.VoiceoverMapping
import org.oppia.proto.v1.structure.AlgebraicExpressionInputInstanceDto
import org.oppia.proto.v1.structure.ChapterSummaryDto
import org.oppia.proto.v1.structure.ConceptCardDto
import org.oppia.proto.v1.structure.ConceptCardLanguagePackDto
import org.oppia.proto.v1.structure.ContentLocalizationDto
import org.oppia.proto.v1.structure.ContentLocalizationsDto
import org.oppia.proto.v1.structure.ContinueInstanceDto
import org.oppia.proto.v1.structure.DownloadableTopicSummaryDto
import org.oppia.proto.v1.structure.DragAndDropSortInputInstanceDto
import org.oppia.proto.v1.structure.ExplorationDto
import org.oppia.proto.v1.structure.ExplorationLanguagePackDto
import org.oppia.proto.v1.structure.FractionDto
import org.oppia.proto.v1.structure.FractionInputInstanceDto
import org.oppia.proto.v1.structure.HintDto
import org.oppia.proto.v1.structure.ImageClickInputInstanceDto
import org.oppia.proto.v1.structure.ImageWithRegionsDto
import org.oppia.proto.v1.structure.ImageWithRegionsDto.LabeledRegionDto
import org.oppia.proto.v1.structure.ImageWithRegionsDto.LabeledRegionDto.NormalizedRectangle2dDto
import org.oppia.proto.v1.structure.ImageWithRegionsDto.LabeledRegionDto.RegionTypeCase.NORMALIZED_RECTANGLE_2D
import org.oppia.proto.v1.structure.InteractionInstanceDto
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
import org.oppia.proto.v1.structure.LocalizableTextDto
import org.oppia.proto.v1.structure.MathEquationInputInstanceDto
import org.oppia.proto.v1.structure.MisconceptionDto
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
import org.oppia.proto.v1.structure.StateDto
import org.oppia.proto.v1.structure.StorySummaryDto
import org.oppia.proto.v1.structure.SubtitledTextDto
import org.oppia.proto.v1.structure.SubtopicSummaryDto
import org.oppia.proto.v1.structure.TextInputInstanceDto
import org.oppia.proto.v1.structure.ThumbnailDto
import org.oppia.proto.v1.structure.TranslatableHtmlContentIdDto
import org.oppia.proto.v1.structure.TranslatableSetOfNormalizedStringDto
import org.oppia.proto.v1.structure.UpcomingTopicSummaryDto
import org.oppia.proto.v1.structure.VoiceoverFileDto

// TODO: For all "not used/unused" properties, remove them from the app's protos.

object DtoProtoToLegacyProtoConverter {
  fun Iterable<DownloadableTopicSummaryDto>.convertToClassroomList(): ClassroomList {
    // TODO: Finish this.
//    val dtos = this
//    return TopicIdList.newBuilder().apply {
//      addAllTopicIds(dtos.map { it.id })
//    }.build()
    return ClassroomList.getDefaultInstance()
  }

  fun DownloadableTopicSummaryDto.convertToTopicRecord(
    imageReferenceReplacements: Map<String, String>
  ): TopicRecord {
    val dto = this
    return TopicRecord.newBuilder().apply {
      this.id = dto.id
      putAllWrittenTranslations(dto.localizations.toTranslationMappings(imageReferenceReplacements))
      this.translatableTitle = dto.localizations.extractDefaultSubtitledHtml(dto.name)
      this.translatableDescription = dto.localizations.extractDefaultSubtitledHtml(dto.description)
      addAllCanonicalStoryIds(dto.storySummariesList.map { it.id })
      addAllSubtopicIds(dto.subtopicSummariesList.map { it.index })
      this.isPublished = true
      this.topicThumbnail = dto.localizations.extractDefaultThumbnail(imageReferenceReplacements)
    }.build()
  }

  fun UpcomingTopicSummaryDto.convertToTopicRecord(
    imageReferenceReplacements: Map<String, String>
  ): TopicRecord {
    val dto = this
    return TopicRecord.newBuilder().apply {
      this.id = dto.id
      putAllWrittenTranslations(dto.localizations.toTranslationMappings(imageReferenceReplacements))
      this.translatableTitle = dto.localizations.extractDefaultSubtitledHtml(dto.name)
      this.translatableDescription = dto.localizations.extractDefaultSubtitledHtml(dto.description)
      this.isPublished = false
      this.topicThumbnail = dto.localizations.extractDefaultThumbnail(imageReferenceReplacements)
    }.build()
  }

  fun StorySummaryDto.convertToStoryRecord(
    imageReferenceReplacements: Map<String, String>
  ): StoryRecord {
    val dto = this
    return StoryRecord.newBuilder().apply {
      this.storyId = dto.id
      putAllWrittenTranslations(dto.localizations.toTranslationMappings(imageReferenceReplacements))
      this.translatableStoryName = dto.localizations.extractDefaultSubtitledHtml(dto.title)
      this.storyThumbnail = dto.localizations.extractDefaultThumbnail(imageReferenceReplacements)
      addAllChapters(dto.chaptersList.map { it.convertToChapterRecord(imageReferenceReplacements) })
    }.build()
  }

  fun RevisionCardDto.convertToSubtopicRecord(
    imageReferenceReplacements: Map<String, String>,
    subtopicSummaryDto: SubtopicSummaryDto,
    languagePackDtos: List<RevisionCardLanguagePackDto>
  ): SubtopicRecord {
    val dto = this
    val localizations = languagePackDtos.map { it.localization }
    return SubtopicRecord.newBuilder().apply {
      this.title = defaultLocalization.extractSubtitledHtml(dto.title)
      this.pageContents = defaultLocalization.extractSubtitledHtml(dto.content)
      putAllRecordedVoiceover(localizations.toVoiceoverMappings())
      putAllWrittenTranslation(localizations.toTranslationMappings(imageReferenceReplacements))
      addAllSkillIds(subtopicSummaryDto.referencedSkillIdsList)
      this.subtopicThumbnail = dto.defaultLocalization.extractThumbnail(imageReferenceReplacements)
    }.build()
  }

  fun convertToConceptCardList(
    allImageReferenceReplacements: Map<String, Map<String, String>>,
    conceptCardDtos: List<Pair<ConceptCardDto, List<ConceptCardLanguagePackDto>>>
  ): ConceptCardList {
    return ConceptCardList.newBuilder().apply {
      addAllConceptCards(
        conceptCardDtos.map { (conceptCard, packs) ->
          val imageReferenceReplacements =
            allImageReferenceReplacements.getValue(conceptCard.skillId)
          conceptCard.convertToConceptCard(imageReferenceReplacements, packs)
        }
      )
    }.build()
  }

  fun ExplorationDto.convertToExploration(
    imageReferenceReplacements: Map<String, String>,
    languagePackDtos: List<ExplorationLanguagePackDto>
  ): Exploration {
    val dto = this
    val localizations = languagePackDtos.map { it.localization }
    // Only top-level content IDs should be present at the exploration level.
    val contentIdTracker = ContentIdTracker(dto.defaultLocalization)
    return Exploration.newBuilder().apply {
      this.id = dto.id
      putAllStates(
        dto.statesMap.mapValues { (name, stateDto) ->
          stateDto.convertToState(
            name, dto.defaultLocalization, localizations, imageReferenceReplacements
          )
        }
      )
      this.initStateName = dto.initStateName
      this.languageCode = dto.defaultLocalization.language.toLegacyLanguageCode()
      this.version = dto.contentVersion
      this.translatableTitle = contentIdTracker.extractSubtitledHtml(dto.title)
      putAllWrittenTranslations(
        localizations.toTranslationMappings(imageReferenceReplacements, contentIdTracker.contentIds)
      )
      // Correctness feedback, description, param changes, and param specs aren't used.
    }.build()
  }

  private fun ChapterSummaryDto.convertToChapterRecord(
    imageReferenceReplacements: Map<String, String>
  ): ChapterRecord {
    val dto = this
    return ChapterRecord.newBuilder().apply {
      this.explorationId = dto.explorationId
      this.chapterThumbnail = dto.localizations.extractDefaultThumbnail(imageReferenceReplacements)
      putAllWrittenTranslations(dto.localizations.toTranslationMappings(imageReferenceReplacements))
      this.translatableTitle = dto.localizations.extractDefaultSubtitledHtml(dto.title)
      this.translatableDescription = dto.localizations.extractDefaultSubtitledHtml(dto.description)
    }.build()
  }

  private fun ConceptCardDto.convertToConceptCard(
    imageReferenceReplacements: Map<String, String>,
    languagePackDtos: List<ConceptCardLanguagePackDto>
  ): ConceptCard {
    val dto = this
    val localizations = languagePackDtos.map { it.localization }
    return ConceptCard.newBuilder().apply {
      this.skillId = dto.skillId
      if (dto.hasDescription()) {
        this.skillDescription = dto.defaultLocalization.extractSubtitledHtml(dto.description).html
      }
      if (dto.hasExplanation()) {
        this.explanation = dto.defaultLocalization.extractSubtitledHtml(dto.explanation)
      }
      addAllWorkedExample(
        dto.workedExamplesList.map { dto.defaultLocalization.extractSubtitledHtml(it.explanation) }
      )
      putAllRecordedVoiceover(localizations.toVoiceoverMappings())
      putAllWrittenTranslation(localizations.toTranslationMappings(imageReferenceReplacements))
    }.build()
  }

  private fun StateDto.convertToState(
    name: String,
    defaultLocalizationDto: ContentLocalizationDto,
    localizations: List<ContentLocalizationDto>,
    imageReferenceReplacements: Map<String, String>
  ): State {
    val dto = this
    // The content IDs associated with translations and voiceovers for a state should only
    // correspond to those actually used within that state (since the new structure stores IDs at
    // the structure level).
    val contentIdTracker = ContentIdTracker(defaultLocalizationDto)
    return State.newBuilder().apply {
      this.name = name
      this.content = contentIdTracker.extractSubtitledHtml(dto.content)
      this.interaction = dto.interaction.convertToInteraction(contentIdTracker)
      putAllRecordedVoiceovers((localizations + defaultLocalizationDto).toVoiceoverMappings(contentIdTracker.contentIds))
      putAllWrittenTranslations(
        localizations.toTranslationMappings(imageReferenceReplacements, contentIdTracker.contentIds)
      )
      // Param changes, linked skill ID, classifier model ID, and answer soliciting aren't used.
    }.build()
  }

  private fun InteractionInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    return when (interactionTypeCase) {
      CONTINUE_INSTANCE -> continueInstance.convertToInteraction(contentIdTracker)
      FRACTION_INPUT -> fractionInput.convertToInteraction(contentIdTracker)
      ITEM_SELECTION_INPUT -> itemSelectionInput.convertToInteraction(contentIdTracker)
      MULTIPLE_CHOICE_INPUT -> multipleChoiceInput.convertToInteraction(contentIdTracker)
      NUMERIC_INPUT -> numericInput.convertToInteraction(contentIdTracker)
      TEXT_INPUT -> textInput.convertToInteraction(contentIdTracker)
      DRAG_AND_DROP_SORT_INPUT -> dragAndDropSortInput.convertToInteraction(contentIdTracker)
      IMAGE_CLICK_INPUT -> imageClickInput.convertToInteraction(contentIdTracker)
      RATIO_EXPRESSION_INPUT -> ratioExpressionInput.convertToInteraction(contentIdTracker)
      ALGEBRAIC_EXPRESSION_INPUT -> algebraicExpressionInput.convertToInteraction(contentIdTracker)
      MATH_EQUATION_INPUT -> mathEquationInput.convertToInteraction(contentIdTracker)
      NUMERIC_EXPRESSION_INPUT -> numericExpressionInput.convertToInteraction(contentIdTracker)
      END_EXPLORATION -> Interaction.newBuilder().setId("EndExploration").build()
      INTERACTIONTYPE_NOT_SET, null -> error("Invalid interaction instance: $this.")
    }
  }

  private fun ContinueInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "Continue"
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap(contentIdTracker))
    }.build()
  }

  private fun ContinueInstanceDto.CustomizationArgsDto.convertToArgsMap(
    contentIdTracker: ContentIdTracker
  ) = mapOf("buttonText" to contentIdTracker.extractSubtitledUnicode(buttonText).wrap())

  private fun FractionInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "FractionInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      dto.solution.takeIf {
        dto.hasSolution()
      }?.convertToSolution(contentIdTracker)?.let { this.solution = it }
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap(contentIdTracker))
    }.build()
  }

  private fun FractionInputInstanceDto.SolutionDto.convertToSolution(
    contentIdTracker: ContentIdTracker
  ): Solution? {
    val dto = this
    return Solution.newBuilder().apply {
      if (dto.baseSolution.hasExplanation()) {
        this.explanation = contentIdTracker.extractSubtitledHtml(dto.baseSolution.explanation)
      }
      this.correctAnswer = dto.correctAnswer.convertToInteractionObject()
      // Whether the answer is exclusive isn't used.
    }.build().takeIf { it != Solution.getDefaultInstance() }
  }

  private fun FractionInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun FractionInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EXACTLY_EQUAL_TO ->
        isExactlyEqualTo.convertToRuleSpec()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUIVALENT_TO ->
        isEquivalentTo.convertToRuleSpec()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUIVALENT_TO_AND_IN_SIMPLEST_FORM ->
        isEquivalentToAndInSimplestForm.convertToRuleSpec()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_LESS_THAN ->
        isLessThan.convertToRuleSpec()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_GREATER_THAN ->
        isGreaterThan.convertToRuleSpec()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_NUMERATOR_EQUAL_TO ->
        hasNumeratorEqualTo.convertToRuleSpec()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_DENOMINATOR_EQUAL_TO ->
        hasDenominatorEqualTo.convertToRuleSpec()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_INTEGER_PART_EQUAL_TO ->
        hasIntegerPartEqualTo.convertToRuleSpec()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_NO_FRACTIONAL_PART ->
        RuleSpec.newBuilder().setRuleType("HasNoFractionalPart").build()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_FRACTIONAL_PART_EXACTLY_EQUAL_TO ->
        hasFractionalPartExactlyEqualTo.convertToRuleSpec()
      FractionInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun FractionInputInstanceDto.RuleSpecDto.IsExactlyEqualToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsExactlyEqualTo"
      putInput("f", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun FractionInputInstanceDto.RuleSpecDto.IsEquivalentToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsEquivalentTo"
      putInput("f", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun FractionInputInstanceDto.RuleSpecDto.IsEquivalentToAndInSimplestFormSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsEquivalentToAndInSimplestForm"
      putInput("f", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun FractionInputInstanceDto.RuleSpecDto.IsLessThanSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsLessThan"
      putInput("f", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun FractionInputInstanceDto.RuleSpecDto.IsGreaterThanSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsGreaterThan"
      putInput("f", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun FractionInputInstanceDto.RuleSpecDto.HasNumeratorEqualToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "HasNumeratorEqualTo"
      putInput("x", dto.input.convertToSignedInteractionObject())
    }.build()
  }

  private fun FractionInputInstanceDto.RuleSpecDto.HasDenominatorEqualToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "HasDenominatorEqualTo"
      putInput("x", dto.input.convertToNonNegativeInteractionObject())
    }.build()
  }

  private fun FractionInputInstanceDto.RuleSpecDto.HasIntegerPartEqualToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "HasIntegerPartEqualTo"
      putInput("x", dto.input.convertToSignedInteractionObject())
    }.build()
  }

  private fun FractionInputInstanceDto.RuleSpecDto.HasFractionalPartExactlyEqualToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "HasFractionalPartExactlyEqualTo"
      putInput("f", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun FractionInputInstanceDto.CustomizationArgsDto.convertToArgsMap(
    contentIdTracker: ContentIdTracker
  ) = mapOf(
    "requireSimplestForm" to requiresSimplestForm.wrap(),
    "allowImproperFraction" to allowImproperFractions.wrap(),
    "allowNonzeroIntegerPart" to allowNonzeroIntegerPart.wrap(),
    "customPlaceholder" to contentIdTracker.extractSubtitledUnicode(placeholder).wrap()
  )

  private fun ItemSelectionInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "ItemSelectionInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap(contentIdTracker))
    }.build()
  }

  private fun ItemSelectionInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun ItemSelectionInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.EQUALS -> equals.convertToRuleSpec()
      ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.CONTAINS_AT_LEAST_ONE_OF ->
        containsAtLeastOneOf.convertToRuleSpec()
      ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.DOES_NOT_CONTAIN_AT_LEAST_ONE_OF ->
        doesNotContainAtLeastOneOf.convertToRuleSpec()
      ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_PROPER_SUBSET_OF ->
        isProperSubsetOf.convertToRuleSpec()
      ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun ItemSelectionInputInstanceDto.RuleSpecDto.EqualsSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "Equals"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun ItemSelectionInputInstanceDto.RuleSpecDto.ContainsAtLeastOneOfSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "ContainsAtLeastOneOf"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun ItemSelectionInputInstanceDto.RuleSpecDto.DoesNotContainAtLeastOneOfSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "DoesNotContainAtLeastOneOf"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun ItemSelectionInputInstanceDto.RuleSpecDto.IsProperSubsetOfSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsProperSubsetOf"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun ItemSelectionInputInstanceDto.CustomizationArgsDto.convertToArgsMap(
    contentIdTracker: ContentIdTracker
  ) = mapOf(
    "minAllowableSelectionCount" to minAllowableSelectionCount.wrap(),
    "maxAllowableSelectionCount" to maxAllowableSelectionCount.wrap(),
    "choices" to choicesList.map { contentIdTracker.extractSubtitledHtml(it).wrap() }.wrap()
  )

  private fun MultipleChoiceInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "MultipleChoiceInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap(contentIdTracker))
    }.build()
  }

  private fun MultipleChoiceInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun MultipleChoiceInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      MultipleChoiceInputInstanceDto.RuleSpecDto.RuleTypeCase.EQUALS -> equals.convertToRuleSpec()
      MultipleChoiceInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun MultipleChoiceInputInstanceDto.RuleSpecDto.EqualsSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "Equals"
      putInput("x", dto.input.convertToNonNegativeInteractionObject())
    }.build()
  }

  private fun MultipleChoiceInputInstanceDto.CustomizationArgsDto.convertToArgsMap(
    contentIdTracker: ContentIdTracker
  ) = mapOf("choices" to choicesList.map { contentIdTracker.extractSubtitledHtml(it).wrap() }.wrap())

  private fun NumericInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "NumericInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      dto.solution.takeIf {
        dto.hasSolution()
      }?.convertToSolution(contentIdTracker)?.let { this.solution = it }
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
    }.build()
  }

  private fun NumericInputInstanceDto.SolutionDto.convertToSolution(
    contentIdTracker: ContentIdTracker
  ): Solution? {
    val dto = this
    return Solution.newBuilder().apply {
      if (dto.baseSolution.hasExplanation()) {
        this.explanation = contentIdTracker.extractSubtitledHtml(dto.baseSolution.explanation)
      }
      this.correctAnswer = dto.correctAnswer.convertToInteractionObject()
      // Whether the answer is exclusive isn't used.
    }.build().takeIf { it != Solution.getDefaultInstance() }
  }

  private fun NumericInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun NumericInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      NumericInputInstanceDto.RuleSpecDto.RuleTypeCase.EQUALS -> equals.convertToRuleSpec()
      NumericInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_LESS_THAN ->
        isLessThan.convertToRuleSpec()
      NumericInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_GREATER_THAN ->
        isGreaterThan.convertToRuleSpec()
      NumericInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_LESS_THAN_OR_EQUAL_TO ->
        isLessThanOrEqualTo.convertToRuleSpec()
      NumericInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_GREATER_THAN_OR_EQUAL_TO ->
        isGreaterThanOrEqualTo.convertToRuleSpec()
      NumericInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_INCLUSIVELY_BETWEEN ->
        isInclusivelyBetween.convertToRuleSpec()
      NumericInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_WITHIN_TOLERANCE ->
        isWithinTolerance.convertToRuleSpec()
      NumericInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun NumericInputInstanceDto.RuleSpecDto.EqualsSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "Equals"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun NumericInputInstanceDto.RuleSpecDto.IsLessThanSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsLessThan"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun NumericInputInstanceDto.RuleSpecDto.IsGreaterThanSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsGreaterThan"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun NumericInputInstanceDto.RuleSpecDto.IsLessThanOrEqualToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsLessThanOrEqualTo"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun NumericInputInstanceDto.RuleSpecDto.IsGreaterThanOrEqualToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsGreaterThanOrEqualTo"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun NumericInputInstanceDto.RuleSpecDto.IsInclusivelyBetweenSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsInclusivelyBetween"
      putInput("a", dto.inputLowerInclusive.convertToInteractionObject())
      putInput("b", dto.inputUpperInclusive.convertToInteractionObject())
    }.build()
  }

  private fun NumericInputInstanceDto.RuleSpecDto.IsWithinToleranceSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsWithinTolerance"
      putInput("tol", dto.inputTolerance.convertToInteractionObject())
      putInput("x", dto.inputComparedValue.convertToInteractionObject())
    }.build()
  }

  private fun TextInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "TextInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      dto.solution.takeIf {
        dto.hasSolution()
      }?.convertToSolution(contentIdTracker)?.let { this.solution = it }
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap(contentIdTracker))
    }.build()
  }

  private fun TextInputInstanceDto.SolutionDto.convertToSolution(
    contentIdTracker: ContentIdTracker
  ): Solution? {
    val dto = this
    return Solution.newBuilder().apply {
      if (dto.baseSolution.hasExplanation()) {
        this.explanation = contentIdTracker.extractSubtitledHtml(dto.baseSolution.explanation)
      }
      this.correctAnswer = dto.correctAnswer.convertToNormalizedStringObject()
      // Whether the answer is exclusive isn't used.
    }.build().takeIf { it != Solution.getDefaultInstance() }
  }

  private fun TextInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec(contentIdTracker) })
      // Training data isn't used.
    }.build()
  }

  private fun TextInputInstanceDto.RuleSpecDto.convertToRuleSpec(
    contentIdTracker: ContentIdTracker
  ): RuleSpec {
    return when (ruleTypeCase) {
      TextInputInstanceDto.RuleSpecDto.RuleTypeCase.EQUALS ->
        equals.convertToRuleSpec(contentIdTracker)
      TextInputInstanceDto.RuleSpecDto.RuleTypeCase.STARTS_WITH ->
        startsWith.convertToRuleSpec(contentIdTracker)
      TextInputInstanceDto.RuleSpecDto.RuleTypeCase.CONTAINS ->
        contains.convertToRuleSpec(contentIdTracker)
      TextInputInstanceDto.RuleSpecDto.RuleTypeCase.FUZZY_EQUALS ->
        fuzzyEquals.convertToRuleSpec(contentIdTracker)
      TextInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun TextInputInstanceDto.RuleSpecDto.EqualsSpecDto.convertToRuleSpec(
    contentIdTracker: ContentIdTracker
  ): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "Equals"
      putInput("x", dto.input.convertToInteractionObject(contentIdTracker))
    }.build()
  }

  private fun TextInputInstanceDto.RuleSpecDto.StartsWithSpecDto.convertToRuleSpec(
    contentIdTracker: ContentIdTracker
  ): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "StartsWith"
      putInput("x", dto.input.convertToInteractionObject(contentIdTracker))
    }.build()
  }

  private fun TextInputInstanceDto.RuleSpecDto.ContainsSpecDto.convertToRuleSpec(
    contentIdTracker: ContentIdTracker
  ): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "Contains"
      putInput("x", dto.input.convertToInteractionObject(contentIdTracker))
    }.build()
  }

  private fun TextInputInstanceDto.RuleSpecDto.FuzzyEqualsSpecDto.convertToRuleSpec(
    contentIdTracker: ContentIdTracker
  ): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "FuzzyEquals"
      putInput("x", dto.input.convertToInteractionObject(contentIdTracker))
    }.build()
  }

  private fun TextInputInstanceDto.CustomizationArgsDto.convertToArgsMap(
    contentIdTracker: ContentIdTracker
  ) = mapOf(
    "placeholder" to contentIdTracker.extractSubtitledUnicode(placeholder).wrap(),
    "rows" to rows.wrap()
  )

  private fun DragAndDropSortInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "DragAndDropSortInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      dto.solution.takeIf {
        dto.hasSolution()
      }?.convertToSolution(contentIdTracker)?.let { this.solution = it }
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap(contentIdTracker))
    }.build()
  }

  private fun DragAndDropSortInputInstanceDto.SolutionDto.convertToSolution(
    contentIdTracker: ContentIdTracker
  ): Solution? {
    val dto = this
    return Solution.newBuilder().apply {
      if (dto.baseSolution.hasExplanation()) {
        this.explanation = contentIdTracker.extractSubtitledHtml(dto.baseSolution.explanation)
      }
      this.correctAnswer = dto.correctAnswer.convertToInteractionObject()
      // Whether the answer is exclusive isn't used.
    }.build().takeIf { it != Solution.getDefaultInstance() }
  }

  private fun DragAndDropSortInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun DragAndDropSortInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUAL_TO_ORDERING ->
        isEqualToOrdering.convertToRuleSpec()
      DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUAL_TO_ORDERING_WITH_ONE_ITEM_AT_INCORRECT_POSITION ->
        isEqualToOrderingWithOneItemAtIncorrectPosition.convertToRuleSpec()
      DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_ELEMENT_X_AT_POSITION_Y ->
        hasElementXAtPositionY.convertToRuleSpec()
      DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_ELEMENT_X_BEFORE_ELEMENT_Y ->
        hasElementXBeforeElementY.convertToRuleSpec()
      DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun DragAndDropSortInputInstanceDto.RuleSpecDto.IsEqualToOrderingSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsEqualToOrdering"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun DragAndDropSortInputInstanceDto.RuleSpecDto.IsEqualToOrderingWithOneItemAtIncorrectPositionSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsEqualToOrderingWithOneItemAtIncorrectPosition"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun DragAndDropSortInputInstanceDto.RuleSpecDto.HasElementXAtPositionYSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "HasElementXAtPositionY"
      putInput("x", dto.element.convertToInteractionObject())
      putInput("y", dto.position.convertToNonNegativeInteractionObject())
    }.build()
  }

  private fun DragAndDropSortInputInstanceDto.RuleSpecDto.HasElementXBeforeElementYSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "HasElementXBeforeElementY"
      putInput("x", dto.consideredElement.convertToInteractionObject())
      putInput("y", dto.laterElement.convertToInteractionObject())
    }.build()
  }

  private fun DragAndDropSortInputInstanceDto.CustomizationArgsDto.convertToArgsMap(
    contentIdTracker: ContentIdTracker
  ) = mapOf(
    "choices" to choicesList.map { contentIdTracker.extractSubtitledHtml(it).wrap() }.wrap(),
    "allowMultipleItemsInSamePosition" to allowMultipleItemsInSamePosition.wrap()
  )

  private fun ImageClickInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "ImageClickInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap())
    }.build()
  }

  private fun ImageClickInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun ImageClickInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      ImageClickInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_IN_REGION ->
        isInRegion.convertToRuleSpec()
      ImageClickInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun ImageClickInputInstanceDto.RuleSpecDto.IsInRegionSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsInRegion"
      putInput("x", dto.inputRegion.convertToNormalizedStringObject())
    }.build()
  }

  private fun ImageClickInputInstanceDto.CustomizationArgsDto.convertToArgsMap() =
    mapOf("imageAndRegions" to imageAndRegions.convertToImageWithRegions().wrap())

  private fun RatioExpressionInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "RatioExpressionInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      dto.solution.takeIf {
        dto.hasSolution()
      }?.convertToSolution(contentIdTracker)?.let { this.solution = it }
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap(contentIdTracker))
    }.build()
  }

  private fun RatioExpressionInputInstanceDto.SolutionDto.convertToSolution(
    contentIdTracker: ContentIdTracker
  ): Solution? {
    val dto = this
    return Solution.newBuilder().apply {
      if (dto.baseSolution.hasExplanation()) {
        this.explanation = contentIdTracker.extractSubtitledHtml(dto.baseSolution.explanation)
      }
      this.correctAnswer = dto.correctAnswer.convertToInteractionObject()
      // Whether the answer is exclusive isn't used.
    }.build().takeIf { it != Solution.getDefaultInstance() }
  }

  private fun RatioExpressionInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun RatioExpressionInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      RatioExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.EQUALS -> equals.convertToRuleSpec()
      RatioExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUIVALENT ->
        isEquivalent.convertToRuleSpec()
      RatioExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_NUMBER_OF_TERMS_EQUAL_TO ->
        hasNumberOfTermsEqualTo.convertToRuleSpec()
      RatioExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_SPECIFIC_TERM_EQUAL_TO ->
        hasSpecificTermEqualTo.convertToRuleSpec()
      RatioExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun RatioExpressionInputInstanceDto.RuleSpecDto.EqualsSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "Equals"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun RatioExpressionInputInstanceDto.RuleSpecDto.IsEquivalentSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsEquivalent"
      putInput("x", dto.input.convertToInteractionObject())
    }.build()
  }

  private fun RatioExpressionInputInstanceDto.RuleSpecDto.HasNumberOfTermsEqualToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "HasNumberOfTermsEqualTo"
      putInput("y", dto.inputTermCount.convertToNonNegativeInteractionObject())
    }.build()
  }

  private fun RatioExpressionInputInstanceDto.RuleSpecDto.HasSpecificTermEqualToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "HasSpecificTermEqualTo"
      putInput("x", dto.inputTermIndex.convertToNonNegativeInteractionObject())
      putInput("y", dto.inputExpectedTermValue.convertToNonNegativeInteractionObject())
    }.build()
  }

  private fun RatioExpressionInputInstanceDto.CustomizationArgsDto.convertToArgsMap(
    contentIdTracker: ContentIdTracker
  ) = mapOf(
    "placeholder" to contentIdTracker.extractSubtitledUnicode(placeholder).wrap(),
    "numberOfTerms" to numberOfTerms.wrap()
  )

  private fun AlgebraicExpressionInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "AlgebraicExpressionInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      dto.solution.takeIf {
        dto.hasSolution()
      }?.convertToSolution(contentIdTracker)?.let { this.solution = it }
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap())
    }.build()
  }

  private fun AlgebraicExpressionInputInstanceDto.SolutionDto.convertToSolution(
    contentIdTracker: ContentIdTracker
  ): Solution? {
    val dto = this
    return Solution.newBuilder().apply {
      if (dto.baseSolution.hasExplanation()) {
        this.explanation = contentIdTracker.extractSubtitledHtml(dto.baseSolution.explanation)
      }
      this.correctAnswer = dto.correctAnswer.convertToMathExpressionObject()
      // Whether the answer is exclusive isn't used.
    }.build().takeIf { it != Solution.getDefaultInstance() }
  }

  private fun AlgebraicExpressionInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun AlgebraicExpressionInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      AlgebraicExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.MATCHES_EXACTLY_WITH ->
        matchesExactlyWith.convertToRuleSpec()
      AlgebraicExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.MATCHES_UP_TO_TRIVIAL_MANIPULATIONS ->
        matchesUpToTrivialManipulations.convertToRuleSpec()
      AlgebraicExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUIVALENT_TO ->
        isEquivalentTo.convertToRuleSpec()
      AlgebraicExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun AlgebraicExpressionInputInstanceDto.RuleSpecDto.MatchesExactlyWithSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "MatchesExactlyWith"
      putInput("x", dto.algebraicExpression.convertToMathExpressionObject())
    }.build()
  }

  private fun AlgebraicExpressionInputInstanceDto.RuleSpecDto.MatchesUpToTrivialManipulationsSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "MatchesUpToTrivialManipulations"
      putInput("x", dto.algebraicExpression.convertToMathExpressionObject())
    }.build()
  }

  private fun AlgebraicExpressionInputInstanceDto.RuleSpecDto.IsEquivalentToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsEquivalentTo"
      putInput("x", dto.algebraicExpression.convertToMathExpressionObject())
    }.build()
  }

  private fun AlgebraicExpressionInputInstanceDto.CustomizationArgsDto.convertToArgsMap() = mapOf(
    "customOskLetters" to customOskLettersList.map { it.wrap() }.wrap(),
    "useFractionForDivision" to useFractionForDivision.wrap()
  )

  private fun MathEquationInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "MathEquationInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      dto.solution.takeIf {
        dto.hasSolution()
      }?.convertToSolution(contentIdTracker)?.let { this.solution = it }
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap())
    }.build()
  }

  private fun MathEquationInputInstanceDto.SolutionDto.convertToSolution(
    contentIdTracker: ContentIdTracker
  ): Solution? {
    val dto = this
    return Solution.newBuilder().apply {
      if (dto.baseSolution.hasExplanation()) {
        this.explanation = contentIdTracker.extractSubtitledHtml(dto.baseSolution.explanation)
      }
      this.correctAnswer = dto.correctAnswer.convertToMathExpressionObject()
      // Whether the answer is exclusive isn't used.
    }.build().takeIf { it != Solution.getDefaultInstance() }
  }

  private fun MathEquationInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun MathEquationInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      MathEquationInputInstanceDto.RuleSpecDto.RuleTypeCase.MATCHES_EXACTLY_WITH ->
        matchesExactlyWith.convertToRuleSpec()
      MathEquationInputInstanceDto.RuleSpecDto.RuleTypeCase.MATCHES_UP_TO_TRIVIAL_MANIPULATIONS ->
        matchesUpToTrivialManipulations.convertToRuleSpec()
      MathEquationInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUIVALENT_TO ->
        isEquivalentTo.convertToRuleSpec()
      MathEquationInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun MathEquationInputInstanceDto.RuleSpecDto.MatchesExactlyWithSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "MatchesExactlyWith"
      putInput("x", dto.mathEquation.convertToMathExpressionObject())
    }.build()
  }

  private fun MathEquationInputInstanceDto.RuleSpecDto.MatchesUpToTrivialManipulationsSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "MatchesUpToTrivialManipulations"
      putInput("x", dto.mathEquation.convertToMathExpressionObject())
    }.build()
  }

  private fun MathEquationInputInstanceDto.RuleSpecDto.IsEquivalentToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsEquivalentTo"
      putInput("x", dto.mathEquation.convertToMathExpressionObject())
    }.build()
  }

  private fun MathEquationInputInstanceDto.CustomizationArgsDto.convertToArgsMap() = mapOf(
    "customOskLetters" to customOskLettersList.map { it.wrap() }.wrap(),
    "useFractionForDivision" to useFractionForDivision.wrap()
  )

  private fun NumericExpressionInputInstanceDto.convertToInteraction(
    contentIdTracker: ContentIdTracker
  ): Interaction {
    val dto = this
    return Interaction.newBuilder().apply {
      this.id = "NumericExpressionInput"
      addAllAnswerGroups(dto.answerGroupsList.map { it.convertToAnswerGroup(contentIdTracker) })
      dto.solution.takeIf {
        dto.hasSolution()
      }?.convertToSolution(contentIdTracker)?.let { this.solution = it }
      addAllHint(dto.hintsList.map { it.convertToOutcome(contentIdTracker) })
      this.defaultOutcome = dto.defaultOutcome.convertToOutcome(contentIdTracker)
      putAllCustomizationArgs(dto.customizationArgs.convertToArgsMap(contentIdTracker))
    }.build()
  }

  private fun NumericExpressionInputInstanceDto.SolutionDto.convertToSolution(
    contentIdTracker: ContentIdTracker
  ): Solution? {
    val dto = this
    return Solution.newBuilder().apply {
      if (dto.baseSolution.hasExplanation()) {
        this.explanation = contentIdTracker.extractSubtitledHtml(dto.baseSolution.explanation)
      }
      this.correctAnswer = dto.correctAnswer.convertToMathExpressionObject()
      // Whether the answer is exclusive isn't used.
    }.build().takeIf { it != Solution.getDefaultInstance() }
  }

  private fun NumericExpressionInputInstanceDto.AnswerGroupDto.convertToAnswerGroup(
    contentIdTracker: ContentIdTracker
  ): AnswerGroup {
    val dto = this
    return AnswerGroup.newBuilder().apply {
      this.taggedSkillMisconception =
        dto.baseAnswerGroup.taggedSkillMisconception.convertToMisconception()
      this.outcome = dto.baseAnswerGroup.outcome.convertToOutcome(contentIdTracker)
      addAllRuleSpecs(dto.ruleSpecsList.map { it.convertToRuleSpec() })
      // Training data isn't used.
    }.build()
  }

  private fun NumericExpressionInputInstanceDto.RuleSpecDto.convertToRuleSpec(): RuleSpec {
    return when (ruleTypeCase) {
      NumericExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.MATCHES_EXACTLY_WITH ->
        matchesExactlyWith.convertToRuleSpec()
      NumericExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.MATCHES_UP_TO_TRIVIAL_MANIPULATIONS ->
        matchesUpToTrivialManipulations.convertToRuleSpec()
      NumericExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUIVALENT_TO ->
        isEquivalentTo.convertToRuleSpec()
      NumericExpressionInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
        error("Invalid rule spec: $this.")
    }
  }

  private fun NumericExpressionInputInstanceDto.RuleSpecDto.MatchesExactlyWithSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "MatchesExactlyWith"
      putInput("x", dto.numericExpression.convertToMathExpressionObject())
    }.build()
  }

  private fun NumericExpressionInputInstanceDto.RuleSpecDto.MatchesUpToTrivialManipulationsSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "MatchesUpToTrivialManipulations"
      putInput("x", dto.numericExpression.convertToMathExpressionObject())
    }.build()
  }

  private fun NumericExpressionInputInstanceDto.RuleSpecDto.IsEquivalentToSpecDto.convertToRuleSpec(): RuleSpec {
    val dto = this
    return RuleSpec.newBuilder().apply {
      this.ruleType = "IsEquivalentTo"
      putInput("x", dto.numericExpression.convertToMathExpressionObject())
    }.build()
  }

  private fun NumericExpressionInputInstanceDto.CustomizationArgsDto.convertToArgsMap(
    contentIdTracker: ContentIdTracker
  ) = mapOf(
    "placeholder" to contentIdTracker.extractSubtitledUnicode(placeholder).wrap(),
    "useFractionForDivision" to useFractionForDivision.wrap()
  )

  private fun OutcomeDto.convertToOutcome(contentIdTracker: ContentIdTracker): Outcome {
    val dto = this
    return Outcome.newBuilder().apply {
      this.destStateName = dto.destinationState
      this.feedback = contentIdTracker.extractSubtitledHtml(dto.feedback)
      this.labelledAsCorrect = dto.labelledAsCorrect
      // Refresher exploration ID, param changes, and prerequisite skill ID are not used.
    }.build()
  }

  private fun HintDto.convertToOutcome(contentIdTracker: ContentIdTracker): Hint {
    val dto = this
    return Hint.newBuilder().apply {
      this.hintContent = contentIdTracker.extractSubtitledHtml(dto.hintContent)
    }.build()
  }

  private fun MisconceptionDto.convertToMisconception(): Misconception {
    val dto = this
    return Misconception.newBuilder().apply {
      this.skillId = dto.skillId
      this.misconceptionId = dto.misconceptionId
    }.build()
  }

  private fun ContentLocalizationsDto.extractDefaultSubtitledHtml(text: SubtitledTextDto) =
    defaultMapping.extractSubtitledHtml(text)

  private fun ContentLocalizationsDto.extractDefaultThumbnail(
    imageReferenceReplacements: Map<String, String>
  ) = defaultMapping.extractThumbnail(imageReferenceReplacements)

  private fun ContentLocalizationsDto.toTranslationMappings(
    imageReferenceReplacements: Map<String, String>
  ) = localizationsList.toTranslationMappings(imageReferenceReplacements)

  private fun ContentLocalizationDto.extractSubtitledHtml(text: SubtitledTextDto): SubtitledHtml =
    localizableTextContentMappingMap.getValue(text.contentId).toSubtitledHtml(text.contentId)

  private fun ContentLocalizationDto.extractSubtitledUnicode(
    text: SubtitledTextDto
  ) = localizableTextContentMappingMap.getValue(text.contentId).toSubtitledUnicode(text.contentId)

  private fun ContentLocalizationDto.extractStringList(contentId: String) =
    localizableTextContentMappingMap.getValue(contentId).toStringList()

  private fun ContentLocalizationDto.extractThumbnail(
    imageReferenceReplacements: Map<String, String>
  ): LessonThumbnail = thumbnail.toThumbnail(imageReferenceReplacements)

  private fun ContentIdTracker.extractSubtitledHtml(text: SubtitledTextDto): SubtitledHtml =
    localizationDto.extractSubtitledHtml(text).also { trackContentId(text.contentId) }

  private fun ContentIdTracker.extractSubtitledUnicode(text: SubtitledTextDto) =
    localizationDto.extractSubtitledUnicode(text).also { trackContentId(text.contentId) }

  private fun ContentIdTracker.extractStringList(contentId: String) =
    localizationDto.extractStringList(contentId).also { trackContentId(contentId) }

  private fun List<ContentLocalizationDto>.toTranslationMappings(
    imageReferenceReplacements: Map<String, String>
  ) = toTranslationMappings(imageReferenceReplacements, filterContentIds = null)

  private fun List<ContentLocalizationDto>.toTranslationMappings(
    imageReferenceReplacements: Map<String, String>,
    filterContentIds: Set<String>?
  ): Map<String, TranslationMapping> {
    return associateUniquely(
      keySelector = { it.language.toLegacyLanguageCode() },
      valueSelector = {
        it.localizableTextContentMappingMap.filterKeys { contentId ->
          filterContentIds == null || contentId in filterContentIds
        }.mapValues { (_, dto) -> dto.toTranslation(imageReferenceReplacements) }
      }
    ).flipMapping().mapValues { (_, languageMap) -> languageMap.toTranslationMapping() }
  }

  private fun List<ContentLocalizationDto>.toVoiceoverMappings(): Map<String, VoiceoverMapping> =
    toVoiceoverMappings(filterContentIds = null)

  private fun List<ContentLocalizationDto>.toVoiceoverMappings(
    filterContentIds: Set<String>?
  ): Map<String, VoiceoverMapping> {
    return associateUniquely(
      // Oppia web currently uses 'pt' for Brazilian Portuguese voiceovers.
      keySelector = { it.language.toLegacyLanguageCode(portugueseOverride = "pt") },
      valueSelector = {
        it.voiceoverContentMappingMap.filterKeys { contentId ->
          filterContentIds == null || contentId in filterContentIds
        }.mapValues { (_, dto) -> dto.toVoiceover() }
      }
    ).flipMapping().mapValues { (_, languageMap) -> languageMap.toVoiceoverMapping() }
  }

  private fun LocalizableTextDto.toSubtitledHtml(contentId: String): SubtitledHtml {
    val dto = this
    require(dto.dataFormatCase == LocalizableTextDto.DataFormatCase.SINGLE_LOCALIZABLE_TEXT) {
      "Error: localizable text is not a single value and can't be converted to SubtitledHtml."
    }
    return SubtitledHtml.newBuilder().apply {
      this.contentId = contentId
      this.html = dto.singleLocalizableText.text
    }.build()
  }

  private fun LocalizableTextDto.toSubtitledUnicode(contentId: String): SubtitledUnicode {
    val dto = this
    require(dto.dataFormatCase == LocalizableTextDto.DataFormatCase.SINGLE_LOCALIZABLE_TEXT) {
      "Error: localizable text is not a single value and can't be converted to SubtitledHtml."
    }
    return SubtitledUnicode.newBuilder().apply {
      this.contentId = contentId
      this.unicodeStr = dto.singleLocalizableText.text
    }.build()
  }

  private fun LocalizableTextDto.toStringList(): List<String> {
    val dto = this
    require(dto.dataFormatCase == LocalizableTextDto.DataFormatCase.SET_OF_LOCALIZABLE_TEXT) {
      "Error: localizable text is not a multi-value and can't be converted to list of strings."
    }
    return dto.setOfLocalizableText.textList
  }

  private fun LocalizableTextDto.toTranslation(
    imageReferenceReplacements: Map<String, String>
  ): Translation {
    val dto = this
    return Translation.newBuilder().apply {
      when (dto.dataFormatCase) {
        LocalizableTextDto.DataFormatCase.SINGLE_LOCALIZABLE_TEXT ->
          this.html = dto.singleLocalizableText.text.fixImageReferences(imageReferenceReplacements)
        LocalizableTextDto.DataFormatCase.SET_OF_LOCALIZABLE_TEXT -> {
          this.htmlList = HtmlTranslationList.newBuilder().apply {
            addAllHtml(
              dto.setOfLocalizableText.textList.fixImageReferences(imageReferenceReplacements)
            )
          }.build()
        }
        LocalizableTextDto.DataFormatCase.DATAFORMAT_NOT_SET, null ->
          error("Invalid localizable text: $dto.")
      }
    }.build()
  }

  private fun VoiceoverFileDto.toVoiceover(): Voiceover {
    val dto = this
    return Voiceover.newBuilder().apply {
      this.fileSizeBytes = dto.fileSizeBytes.toLong()
      this.fileName = dto.filename
    }.build()
  }

  private fun ThumbnailDto.toThumbnail(
    imageReferenceReplacements: Map<String, String>
  ): LessonThumbnail {
    val dto = this
    val oldFilename = dto.referencedImage.filename
    return LessonThumbnail.newBuilder().apply {
      this.thumbnailFilename = imageReferenceReplacements[oldFilename] ?: oldFilename
      this.backgroundColorRgb = dto.backgroundColorRgb
    }.build()
  }

  private fun LanguageType.toLegacyLanguageCode(portugueseOverride: String? = null): String {
    return when (this) {
      LanguageType.ENGLISH -> "en"
      LanguageType.ARABIC -> "ar"
      LanguageType.HINDI -> "hi"
      LanguageType.HINGLISH -> "hi-en"
      LanguageType.BRAZILIAN_PORTUGUESE -> portugueseOverride ?: "pt-BR"
      LanguageType.SWAHILI -> "sw"
      LanguageType.NIGERIAN_PIDGIN -> "pcm"
      LanguageType.LANGUAGE_CODE_UNSPECIFIED, LanguageType.UNRECOGNIZED ->
        error("Invalid language code: $this.")
    }
  }

  private fun Map<String, Translation>.toTranslationMapping(): TranslationMapping {
    return TranslationMapping.newBuilder().apply {
      putAllTranslationMapping(this@toTranslationMapping)
    }.build()
  }

  private fun Map<String, Voiceover>.toVoiceoverMapping(): VoiceoverMapping =
    VoiceoverMapping.newBuilder().apply { putAllVoiceoverMapping(this@toVoiceoverMapping) }.build()

  private fun ImageWithRegionsDto.convertToImageWithRegions(): ImageWithRegions {
    val dto = this
    return ImageWithRegions.newBuilder().apply {
      this.imagePath = dto.imageFilePath
      addAllLabelRegions(dto.labeledRegionsList.map { it.convertToLabeledRegion() })
    }.build()
  }

  private fun LabeledRegionDto.convertToLabeledRegion(): LabeledRegion {
    val dto = this
    return LabeledRegion.newBuilder().apply {
      this.label = dto.label
      check(dto.regionTypeCase == NORMALIZED_RECTANGLE_2D) { "Invalid region: $dto." }
      this.region = LabeledRegion.Region.newBuilder().apply {
        this.regionType = LabeledRegion.Region.RegionType.RECTANGLE
        this.area = dto.normalizedRectangle2D.convertToLabeledRegion()
      }.build()
    }.build()
  }

  private fun NormalizedRectangle2dDto.convertToLabeledRegion(): NormalizedRectangle2d {
    val dto = this
    return NormalizedRectangle2d.newBuilder().apply {
      this.upperLeft = dto.topLeft.convertToPoint2d()
      this.lowerRight = dto.bottomRight.convertToPoint2d()
    }.build()
  }

  private fun NormalizedPoint2dDto.convertToPoint2d(): Point2d {
    val dto = this
    return Point2d.newBuilder().apply {
      this.x = dto.x.toFloat()
      this.y = dto.y.toFloat()
    }.build()
  }

  private fun String.convertToNormalizedStringObject(): InteractionObject =
    InteractionObject.newBuilder().setNormalizedString(this).build()

  private fun Int.convertToSignedInteractionObject(): InteractionObject =
    InteractionObject.newBuilder().setSignedInt(this).build()

  private fun Int.convertToNonNegativeInteractionObject(): InteractionObject =
    InteractionObject.newBuilder().setNonNegativeInt(this).build()

  private fun Double.convertToInteractionObject(): InteractionObject =
    InteractionObject.newBuilder().setReal(this).build()

  private fun FractionDto.convertToInteractionObject(): InteractionObject =
    InteractionObject.newBuilder().setFraction(convertToFraction()).build()

  private fun RatioExpressionDto.convertToInteractionObject(): InteractionObject =
    InteractionObject.newBuilder().setRatioExpression(convertToRatioExpression()).build()

  private fun TranslatableSetOfNormalizedStringDto.convertToInteractionObject(
    contentIdTracker: ContentIdTracker
  ): InteractionObject {
    val dto = this
    return InteractionObject.newBuilder().apply {
      this.translatableSetOfNormalizedString = dto.convertToSetOfNormalizedString(contentIdTracker)
    }.build()
  }

  private fun TranslatableHtmlContentIdDto.convertToInteractionObject(): InteractionObject =
    InteractionObject.newBuilder().setTranslatableHtmlContentId(convertToTranslatableContentId()).build()

  private fun SetOfTranslatableHtmlContentIdsDto.convertToInteractionObject(): InteractionObject =
    InteractionObject.newBuilder().setSetOfTranslatableHtmlContentIds(convertToSetOfTranslatableContentIds()).build()

  private fun ListOfSetsOfTranslatableHtmlContentIdsDto.convertToInteractionObject(): InteractionObject =
    InteractionObject.newBuilder().setListOfSetsOfTranslatableHtmlContentIds(convertToListOfSetsOfTranslatableContentIds()).build()

  private fun String.convertToMathExpressionObject(): InteractionObject =
    InteractionObject.newBuilder().setMathExpression(this).build()

  private fun FractionDto.convertToFraction(): Fraction {
    val dto = this
    return Fraction.newBuilder().apply {
      this.isNegative = dto.isNegative
      this.wholeNumber = dto.wholeNumber
      this.numerator = dto.numerator
      this.denominator = dto.denominator
    }.build()
  }

  private fun RatioExpressionDto.convertToRatioExpression(): RatioExpression =
    RatioExpression.newBuilder().addAllRatioComponent(componentsList).build()

  private fun TranslatableSetOfNormalizedStringDto.convertToSetOfNormalizedString(
    contentIdTracker: ContentIdTracker
  ): TranslatableSetOfNormalizedString {
    val dto = this
    return TranslatableSetOfNormalizedString.newBuilder().apply {
      this.contentId = dto.contentId
      addAllNormalizedStrings(contentIdTracker.extractStringList(dto.contentId))
    }.build()
  }

  private fun TranslatableHtmlContentIdDto.convertToTranslatableContentId() =
    TranslatableHtmlContentId.newBuilder().setContentId(contentId).build()

  private fun SetOfTranslatableHtmlContentIdsDto.convertToSetOfTranslatableContentIds(): SetOfTranslatableHtmlContentIds {
    val dto = this
    return SetOfTranslatableHtmlContentIds.newBuilder().apply {
      addAllContentIds(dto.contentIdsList.map { it.convertToTranslatableContentId() })
    }.build()
  }

  private fun ListOfSetsOfTranslatableHtmlContentIdsDto.convertToListOfSetsOfTranslatableContentIds(): ListOfSetsOfTranslatableHtmlContentIds {
    val dto = this
    return ListOfSetsOfTranslatableHtmlContentIds.newBuilder().apply {
      addAllContentIdLists(dto.contentIdSetsList.map { it.convertToSetOfTranslatableContentIds() })
    }.build()
  }

  private fun SubtitledUnicode.wrap(): SchemaObject =
    SchemaObject.newBuilder().apply { this.subtitledUnicode = this@wrap }.build()

  private fun SubtitledHtml.wrap(): SchemaObject {
    return SchemaObject.newBuilder().apply {
      this.customSchemaValue = CustomSchemaValue.newBuilder().apply {
        this.subtitledHtml = this@wrap
      }.build()
    }.build()
  }

  private fun Boolean.wrap(): SchemaObject =
    SchemaObject.newBuilder().apply { this.boolValue = this@wrap }.build()

  private fun Int.wrap(): SchemaObject =
    SchemaObject.newBuilder().apply { this.signedInt = this@wrap }.build()

  private fun String.wrap(): SchemaObject =
    SchemaObject.newBuilder().apply { this.normalizedString = this@wrap }.build()

  private fun ImageWithRegions.wrap(): SchemaObject {
    return SchemaObject.newBuilder().apply {
      this.customSchemaValue = CustomSchemaValue.newBuilder().apply {
        this.imageWithRegions = this@wrap
      }.build()
    }.build()
  }

  private fun List<SchemaObject>.wrap(): SchemaObject {
    return SchemaObject.newBuilder().apply {
      this.schemaObjectList = SchemaObjectList.newBuilder().apply {
        addAllSchemaObject(this@wrap)
      }.build()
    }.build()
  }

  private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
  private const val CUSTOM_MATH_TAG = "oppia-noninteractive-math"

  private fun String.fixImageReferences(imageReferenceReplacements: Map<String, String>): String =
    fixImageTags(imageReferenceReplacements).fixMathTags(imageReferenceReplacements)

  private fun Iterable<String>.fixImageReferences(
    imageReferenceReplacements: Map<String, String>
  ): List<String> = map { it.fixImageReferences(imageReferenceReplacements) }

  private fun String.fixImageTags(imageReferenceReplacements: Map<String, String>): String =
    fixTags(CUSTOM_IMG_TAG, imageReferenceReplacements)

  private fun String.fixMathTags(imageReferenceReplacements: Map<String, String>): String =
    fixTags(CUSTOM_MATH_TAG, imageReferenceReplacements)

  private fun String.fixTags(tag: String, imageReferenceReplacements: Map<String, String>): String {
    // Replace tags in reverse order to avoid invalidating ranges.
    return findTags(tag).reversed().fold(this) { updatedStr, nextElementRange ->
      updatedStr.replaceReferences(nextElementRange, imageReferenceReplacements)
    }
  }

  private fun String.replaceReferences(
    placement: IntRange, imageReferenceReplacements: Map<String, String>
  ): String {
    return substring(placement).let { element ->
      // This could be done much more efficiently by extracting the image reference.
      imageReferenceReplacements.entries.find { (needle, _) ->
        needle in element
      }?.let { (needle, replacement) -> element.replace(needle, replacement) }
    }?.let { replaceRange(placement, it) } ?: this
  }

  private fun String.findTags(tag: String): List<IntRange> {
    return generateSequence(findNextTag(tag, startFrom = 0)) { previousTagRange ->
      findNextTag(tag, previousTagRange.last + 1)
    }.toList()
  }

  private fun String.findNextTag(tag: String, startFrom: Int): IntRange? {
    val startTagIndex = indexOf("<$tag", startFrom).takeIf { it != -1 } ?: return null
    val endTagIndex1 = indexOf("/>", startTagIndex).takeIf { it != -1 }
    val endTagIndex2 = indexOf("</$tag", startTagIndex).takeIf { it != -1 }
    val endTagIndex = when {
      endTagIndex1 != null && endTagIndex2 != null -> endTagIndex1.coerceAtMost(endTagIndex2)
      endTagIndex1 != null -> endTagIndex1
      endTagIndex2 != null -> endTagIndex2
      else -> return null
    }
    return startTagIndex until endTagIndex
  }

  private fun <T, K, V> Iterable<T>.associateUniquely(
    keySelector: (T) -> K, valueSelector: (T) -> V
  ): Map<K, V> {
    return groupBy(keySelector, valueSelector).mapValues { (key, values) ->
      values.singleOrNull() ?: error("Error: $key was present more than once in collection.")
    }
  }

  private fun <K1, K2, V> Map<K1, Map<K2, V>>.flipMapping(): Map<K2, Map<K1, V>> {
    // First, create the outer map with all possible keys.
    val allNewOuterKeys = values.flatMapTo(mutableSetOf()) { it.keys }
    return allNewOuterKeys.associateWith { mutableMapOf<K1, V>() }.also { newOuterMap ->
      // Next, iterate across all previous inner maps to create the new entries.
      entries.forEach { (prevOuterKey, prevInnerMap) ->
        prevInnerMap.entries.forEach { (prevInnerKey, value) ->
          newOuterMap.getValue(prevInnerKey)[prevOuterKey] = value
        }
      }
    }
  }

  private class ContentIdTracker(val localizationDto: ContentLocalizationDto) {
    private val internalContentIds by lazy { mutableSetOf<String>() }
    val contentIds: Set<String> get() = internalContentIds

    fun trackContentId(contentId: String) {
      internalContentIds += contentId
    }
  }
}

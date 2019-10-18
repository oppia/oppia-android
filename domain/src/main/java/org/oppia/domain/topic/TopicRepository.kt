package org.oppia.domain.topic

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.ConceptCard
import org.oppia.app.model.Exploration
import org.oppia.app.model.ExplorationAssetPackage
import org.oppia.app.model.Interaction
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.ObjectType
import org.oppia.app.model.Outcome
import org.oppia.app.model.ParamChange
import org.oppia.app.model.ParamChangeCustomizationArgs
import org.oppia.app.model.ParamSpec
import org.oppia.app.model.RuleSpec
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.State
import org.oppia.app.model.Story
import org.oppia.app.model.StoryNode
import org.oppia.app.model.StorySummary
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Topic
import org.oppia.app.model.TopicDatabase
import org.oppia.app.model.TopicPackage
import org.oppia.app.model.TopicThumbnailType
import org.oppia.app.model.TrainingData
import org.oppia.app.model.Translation
import org.oppia.app.model.TranslationMapping
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.data.backends.gae.api.ConceptCardService
import org.oppia.data.backends.gae.api.ExplorationService
import org.oppia.data.backends.gae.api.StoryService
import org.oppia.data.backends.gae.api.TopicService
import org.oppia.data.backends.gae.model.GaeAnswerGroup
import org.oppia.data.backends.gae.model.GaeConceptCard
import org.oppia.data.backends.gae.model.GaeCustomizationArgs
import org.oppia.data.backends.gae.model.GaeExploration
import org.oppia.data.backends.gae.model.GaeExplorationContainer
import org.oppia.data.backends.gae.model.GaeInteractionInstance
import org.oppia.data.backends.gae.model.GaeOutcome
import org.oppia.data.backends.gae.model.GaeParamChange
import org.oppia.data.backends.gae.model.GaeParamSpec
import org.oppia.data.backends.gae.model.GaeRecordedVoiceovers
import org.oppia.data.backends.gae.model.GaeRuleSpec
import org.oppia.data.backends.gae.model.GaeSkillContents
import org.oppia.data.backends.gae.model.GaeState
import org.oppia.data.backends.gae.model.GaeStory
import org.oppia.data.backends.gae.model.GaeStoryNode
import org.oppia.data.backends.gae.model.GaeStorySummary
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import org.oppia.data.backends.gae.model.GaeTopic
import org.oppia.data.backends.gae.model.GaeVoiceover
import org.oppia.data.backends.gae.model.GaeWrittenTranslation
import org.oppia.data.backends.gae.model.GaeWrittenTranslations
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncDataSubscriptionManager
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.threading.BackgroundDispatcher
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

private const val TOPIC_PACKAGE_DATA_PROVIDER_PREFIX = "TopicPackageDataProvider_"

@Singleton
class TopicRepository @Inject constructor(
  persistentCacheStoryFactory: PersistentCacheStore.Factory,
  @BackgroundDispatcher backgroundDispatcher: CoroutineDispatcher, private val topicService: TopicService,
  private val storyService: StoryService, private val conceptCardService: ConceptCardService,
  private val explorationService: ExplorationService, private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager
) {
  private val backgroundScope = CoroutineScope(backgroundDispatcher)
  private val failureMap: MutableMap<String, Throwable> = mutableMapOf()

  private val cacheStory: PersistentCacheStore<TopicDatabase> by lazy {
    persistentCacheStoryFactory.create("topic_database", TopicDatabase.getDefaultInstance())
  }

  fun lookUpTopicPackage(topicName: String): DataProvider<TopicPackage> {
    return dataProviders.transformAsync(TOPIC_PACKAGE_DATA_PROVIDER_PREFIX + topicName, cacheStory) { topicDatabase ->
      retrieveOrDownloadTopic(topicName, topicDatabase)
    }
  }

  private fun retrieveOrDownloadTopic(topicName: String, topicDatabase: TopicDatabase): AsyncResult<TopicPackage> {
    return when (topicName) {
      in topicDatabase.topicPackageMap -> AsyncResult.success(topicDatabase.topicPackageMap.getValue(topicName))
      in failureMap -> AsyncResult.failed(failureMap.getValue(topicName))
      else -> {
        scheduleDownloadTopic(topicName)
        AsyncResult.pending()
      }
    }
  }

  private fun scheduleDownloadTopic(topicName: String) {
    backgroundScope.launch {
    }.invokeOnCompletion { failure ->
      failure?.let {
        notifyFailure(topicName, failure)
      }
    }
  }

  private suspend fun downloadTopicPackage(topicName: String): TopicPackage {
    val response = topicService.getTopicByName(topicName).execute()
    if (response.isSuccessful) {
      val topic = checkNotNull(convertGaeTopicToProto(topicName, response.body())) {
        "Expected valid payload for topic $topicName"
      }
      val stories = topic.storyList.map(StorySummary::getStoryId).map { storyId ->
        downloadStory(topicName, storyId)
      }
      val conceptCards = downloadConceptCards(topicName, topic.skillList.map(SkillSummary::getSkillId))
      val explorations =
        topic.storyList.map(StorySummary::getChapterList).flatten().map(ChapterSummary::getExplorationId).distinct()
          .map { explorationId ->
            downloadExploration(topicName, explorationId)
          }
      val explorationAssets = explorations.associate { exploration ->
        exploration.id to downloadExplorationAssets(topicName, exploration)
      }
      return TopicPackage.newBuilder()
        .setTopic(topic)
        .putAllStory(stories.associate { story -> story.storyId to story })
        .putAllConceptCard(conceptCards)
        .putAllExploration(explorations.associate { exploration -> exploration.id to exploration })
        .putAllExplorationAssetPackage(explorationAssets)
        .build()
    } else {
      throw Exception(
        "HTTP failure ${response.code()} when downloading topic $topicName. Error body:" +
            " ${response.errorBody()?.string()}"
      )
    }
  }

  private suspend fun downloadStory(topicName: String, storyId: String): Story {
    val response = storyService.getStory(storyId, /* userId= */ null, /* user= */ null).execute()
    if (response.isSuccessful) {
      return checkNotNull(convertGaeStoryToProto(storyId, response.body())) {
        "Expected valid payload for story $storyId in topic $topicName"
      }
    } else {
      throw Exception(
        "HTTP failure ${response.code()} when downloading story $storyId in topic $topicName. Error body:" +
            " ${response.errorBody()?.string()}"
      )
    }
  }

  private suspend fun downloadConceptCards(topicName: String, skillIds: List<String>): Map<String, ConceptCard> {
    val response = conceptCardService.getSkillContents(skillIds.joinToString(separator = ",")).execute()
    if (response.isSuccessful) {
      return checkNotNull(convertGaeConceptCardToConceptCardMap(topicName, skillIds, response.body())) {
        "Expected valid payload for skill contents for skills $skillIds in topic $topicName"
      }
    } else {
      throw Exception(
        "HTTP failure ${response.code()} when downloading concept cards for skills $skillIds in topic $topicName." +
            " Error body: ${response.errorBody()?.string()}"
      )
    }
  }

  private suspend fun downloadExploration(topicName: String, explorationId: String): Exploration {

  }

  private suspend fun downloadExplorationAssets(topicName: String, exploration: Exploration): ExplorationAssetPackage {

  }

  private fun notifyFailure(topicName: String, failure: Throwable) {
    failureMap[topicName] = failure
    asyncDataSubscriptionManager.notifyChangeAsync(TOPIC_PACKAGE_DATA_PROVIDER_PREFIX + topicName)
  }

  // TODO(BenHenning): Move these to converter classes.
  companion object {
    private fun convertGaeTopicToProto(topicName: String, gaeTopic: GaeTopic?): Topic? {
      if (gaeTopic == null) {
        return null
      }
      val topicId = checkElemPresent(gaeTopic.topicId) { "Missing topic_id for topic $topicName" }
      return Topic.newBuilder()
        .setTopicId(topicId)
        .setName(checkElemPresent(gaeTopic.topicName) { "Missing topic_name from topic $topicName" })
        .setTopicThumbnail(generateTopicThumbnail(gaeTopic.topicName.hashCode()))
        .addAllStory(getCanonicalStoryDicts(topicId, gaeTopic).map { gaeStory ->
          convertGaeStorySummaryToProto(topicId, gaeStory)
        })
        .addAllSkill(getSkillDescriptions(topicId, gaeTopic).map { (skillId, description) ->
          convertGaeSkillDescriptionToProto(skillId, description)
        })
        .build()
    }

    private fun convertGaeStoryToProto(storyId: String, gaeStory: GaeStory?): Story? {
      if (gaeStory == null) {
        return null
      }
      return Story.newBuilder()
        .setStoryId(storyId)
        .setTitle(checkElemPresent(gaeStory.storyTitle) { "Missing story_title from story $storyId" })
        .setDescription(checkElemPresent(gaeStory.storyDescription) { "Missing story_description from story $storyId" })
        .addAllStoryNode(getGaeStoryNodes(storyId, gaeStory.storyNodes).map { gaeStoryNode ->
          convertGaeStoryNodeToProto(storyId, gaeStoryNode)
        })
        .build()
    }

    private fun convertGaeConceptCardToConceptCardMap(
      topicName: String, skillIds: List<String>, gaeConceptCard: GaeConceptCard?
    ): Map<String, ConceptCard>? {
      if (gaeConceptCard == null) {
        return null
      }
      val gaeSkillContentsList = checkElemPresent(gaeConceptCard.conceptCardDicts) {
        "Missing concept_card_dicts from content cards retrieved for skills $skillIds in topic $topicName"
      }
      check(skillIds.size == gaeSkillContentsList.size) {
        "Expected concept cards list returned from backend to have same number of elements as skill IDs passed in:" +
            " $skillIds (for topic $topicName)"
      }
      // The backend guarantees that the order of the provided skill contents matches the order of the skill IDs passed
      // to the service.
      return skillIds.zip(gaeSkillContentsList).toMap().mapValues { (skillId, gaeSkillContents) ->
        convertGaeSkillContentsToProto(skillId, gaeSkillContents)
      }
    }

    private fun convertGaeExplorationContainerToProto(
      topicName: String, gaeExplorationContainer: GaeExplorationContainer?
    ): Exploration? {
      if (gaeExplorationContainer == null) {
        return null
      }
      val explorationId = checkElemPresent(gaeExplorationContainer.explorationId) {
        "Missing exploration_id for exploration retrieved from topic $topicName"
      }
      val gaeExploration = checkElemPresent(gaeExplorationContainer.exploration) {
        "Missing exploration for exploration $explorationId retrieved from topic $topicName"
      }
      return Exploration.newBuilder()
        .setId(explorationId)
        .setTitle(checkElemPresent(gaeExploration.title) { "Missing title from exploration $explorationId" })
        .setObjective(checkElemPresent(gaeExploration.objective) {
          "Missing objective from exploration $explorationId"
        })
        .putAllState()
        .addAllParamChange(checkElemPresent(gaeExploration.paramChanges) {
          "Missing param_changes in exploration $explorationId"
        }.map { gaeParamChange -> convertGaeParamChangeToProto(explorationId, gaeParamChange) })
        .putAllParamSpec(checkElemPresent(gaeExploration.paramSpecs) {
          "Missing param_specs in exploration $explorationId"
        }.mapValues { (_, gaeParamSpec) -> convertGaeParamSpecToProto(explorationId, gaeParamSpec) })
        .setLanguageCode(checkElemPresent(gaeExploration.languageCode) {
          "Missing language_code from exploration $explorationId"
        })
        .setCorrectnessFeedbackEnabled(checkElemPresent(gaeExploration.isCorrectnessFeedbackEnabled) {
          "Missing correctness_feedback_enabled from exploration $explorationId"
        })
        .build()
    }

    private fun getCanonicalStoryDicts(topicId: String, gaeTopic: GaeTopic): List<GaeStorySummary?> {
      return checkElemPresent(gaeTopic.canonicalStoryDicts) {
        "Missing canonical_story_dicts from topic $topicId"
      }
    }

    private fun getSkillDescriptions(topicId: String, gaeTopic: GaeTopic): Map<String, String?> {
      return checkElemPresent(gaeTopic.skillDescriptions) {
        "Missing skill_descriptions from topic $topicId"
      }
    }

    private fun convertGaeStorySummaryToProto(topicId: String, gaeStorySummary: GaeStorySummary?): StorySummary {
      val gaeStory = checkElemPresent(gaeStorySummary) { "Missing story summary in dict for topic $topicId" }
      return StorySummary.newBuilder()
        .setStoryId(checkElemPresent(gaeStory.storyId) { "Missing story summary ID from topic $topicId" })
        .setStoryName(checkElemPresent(gaeStory.title) { "Missing story summary title from topic $topicId" })
        // TODO: add ChapterSummary
        .build()
    }

    private fun convertGaeSkillDescriptionToProto(skillId: String, skillDescription: String?): SkillSummary {
      return SkillSummary.newBuilder()
        .setSkillId(skillId)
        .setDescription(checkElemPresent(skillDescription) { "Missing title from skill $skillId" })
        .build()
    }

    private fun getGaeStoryNodes(storyId: String, gaeStoryNodes: List<GaeStoryNode>?): List<GaeStoryNode> {
      return checkElemPresent(gaeStoryNodes) { "Missing story_node from story $storyId" }
    }

    private fun convertGaeStoryNodeToProto(storyId: String, gaeStoryNode: GaeStoryNode?): StoryNode {
      val gaeNode = checkElemPresent(gaeStoryNode) { "Missing story node in list within story $storyId" }
      val nodeId = checkElemPresent(gaeNode.id) { "Missing node id in story $storyId" }
      return StoryNode.newBuilder()
        .setNodeId(nodeId)
        .setTitle(checkElemPresent(gaeNode.title) { "Missing title for node $nodeId in story $storyId" })
        .addAllDestinationNodeId(checkElemPresent(gaeNode.destinationNodeIds) {
          "Missing destination_node_ids for node $nodeId in story $storyId"
        })
        .addAllAcquiredSkillId(checkElemPresent(gaeNode.acquiredSkillIds) {
          "Missing acquired_skill_ids for node $nodeId in story $storyId"
        })
        .setOutline(checkElemPresent(gaeNode.outline) { "Missing outline for node $nodeId in story $storyId" })
        .setOutlineIsFinalized(checkElemPresent(gaeNode.isOutlineFinalized) {
          "Missing outline_is_finalized for node $nodeId in story $storyId"
        })
        .setExplorationId(checkElemPresent(gaeNode.explorationId) {
          "Missing exploration_id for node $nodeId in story $storyId"
        })
        // exp summary
        .setIsCompleted(checkElemPresent(gaeNode.isCompleted) {
          "Missing is_completed for node $nodeId in story $storyId"
        })
        .build()
    }

    private fun convertGaeSkillContentsToProto(skillId: String, gaeSkillContents: GaeSkillContents): ConceptCard {
      return ConceptCard.newBuilder()
        .setSkillId(skillId)
        // TODO: skill description
        .setExplanation(convertGaeSubtitledHtmlToProto(checkElemPresent(gaeSkillContents.explanation) {
          "Missing explanation for contents correspond to skill $skillId"
        }))
        .addAllWorkedExample(checkElemPresent(gaeSkillContents.workedExamples) {
          "Missing worked_examples for contents corresponding to skill $skillId"
        }.map(::convertGaeSubtitledHtmlToProto))
        .putAllRecordedVoiceover(convertGaeRecordedVoiceoversToProtoMap(gaeSkillContents.recordedVoiceovers) {
          "skill $skillId"
        })
        .putAllWrittenTranslation(
          convertGaeWrittenTranslationsToProtoMap(gaeSkillContents.writtenTranslations) {
            "skill $skillId"
          }
        )
        .build()
    }

    private fun convertGaeSubtitledHtmlToProto(gaeSubtitledHtml: GaeSubtitledHtml): SubtitledHtml {
      return SubtitledHtml.newBuilder()
        .setHtml(checkElemPresent(gaeSubtitledHtml.html) { "Missing html in SubtitledHtml" })
        .setContentId(checkElemPresent(gaeSubtitledHtml.contentId) { "Missing content_id in SubtitledHtml" })
        .build()
    }

    private fun convertGaeRecordedVoiceoversToProtoMap(
      gaeRecordedVoiceovers: GaeRecordedVoiceovers?, containerTextFun: () -> String
    ): Map<String, VoiceoverMapping> {
      val gaeVoiceovers = checkElemPresent(gaeRecordedVoiceovers) {
        "Missing recorded_voiceovers for ${containerTextFun()}"
      }
      val gaeVoiceoversMapping = checkElemPresent(gaeVoiceovers.voiceoversMapping) {
        "Missing voiceovers_mapping for ${containerTextFun()}"
      }
      // Convert Map<String, Map<String, GaeVoiceover>> to Map<String, VoiceoverMapping>, where each VoiceoverMapping
      // contains a Map<String, Voiceover>.
      return gaeVoiceoversMapping.mapValues { (_, gaeContentMap) ->
        VoiceoverMapping.newBuilder()
          .putAllVoiceoverMapping(gaeContentMap.mapValues { (contentId, gaeVoiceover) ->
            convertGaeVoiceoverToProto(contentId, gaeVoiceover, containerTextFun)
          })
          .build()
      }
    }

    private fun convertGaeVoiceoverToProto(
      contentId: String, gaeVoiceover: GaeVoiceover, containerTextFun: () -> String
    ): Voiceover {
      return Voiceover.newBuilder()
        .setFileSizeBytes(checkElemPresent(gaeVoiceover.fileSizeBytes) {
          "Missing file_size_bytes from voiceover corresponding to $contentId in ${containerTextFun()}"
        })
        .setNeedsUpdate(checkElemPresent(gaeVoiceover.isUpdateNeeded) {
          "Missing needs_update from voiceover corresponding to $contentId in ${containerTextFun()}"
        })
        .setFileName(checkElemPresent(gaeVoiceover.filename) {
          "Missing filename from voiceover corresponding to $contentId in ${containerTextFun()}"
        })
        .build()
    }

    private fun convertGaeWrittenTranslationsToProtoMap(
      gaeWrittenTranslations: GaeWrittenTranslations?, containerTextFun: () -> String
    ): Map<String, TranslationMapping> {
      val gaeTranslations = checkElemPresent(gaeWrittenTranslations) {
        "Missing written_translations for ${containerTextFun()}"
      }
      val gaeTranslationsMapping = checkElemPresent(gaeTranslations.translationsMapping) {
        "Missing translations_mapping for ${containerTextFun()}"
      }
      // Convert Map<String, Map<String, GaeWrittenTranslation>> to Map<String, TranslationMapping>, where each
      // TranslationMapping contains a Map<String, Translation>.
      return gaeTranslationsMapping.mapValues { (_, gaeContentMap) ->
        TranslationMapping.newBuilder()
          .putAllTranslationMapping(gaeContentMap.mapValues { (contentId, gaeTranslation) ->
            convertGaeWrittenTranslationToProto(contentId, gaeTranslation, containerTextFun)
          })
          .build()
      }
    }

    private fun convertGaeWrittenTranslationToProto(
      contentId: String, gaeWrittenTranslation: GaeWrittenTranslation, containerTextFun: () -> String
    ): Translation {
      return Translation.newBuilder()
        .setHtml(checkElemPresent(gaeWrittenTranslation.html) {
          "Missing html from written translation corresponding to $contentId in ${containerTextFun()}"
        })
        .setNeedsUpdate(checkElemPresent(gaeWrittenTranslation.isUpdateNeeded) {
          "Missing needs_update from written translation corresponding to $contentId in ${containerTextFun()}"
        })
        .build()
    }

    private fun convertGaeStateToProto(explorationId: String, stateName: String, gaeState: GaeState): State {
      return State.newBuilder()
        .setName(stateName)
        .putAllRecordedVoiceovers(convertGaeRecordedVoiceoversToProtoMap(gaeState.recordedVoiceovers) {
          "state $stateName in exploration $explorationId"
        })
        .setContent(convertGaeSubtitledHtmlToProto(checkElemPresent(gaeState.content) {
          "Missing content in state $stateName in exploration $explorationId"
        }))
        .putAllWrittenTranslations(convertGaeWrittenTranslationsToProtoMap(gaeState.writtenTranslations) {
          "state $stateName in exploration $explorationId"
        })
        .addAllParamChanges(checkElemPresent(gaeState.paramChanges) {
          "Missing param_changes in state $stateName in exploration $explorationId"
        }.map { gaeParamChange -> convertGaeParamChangeToProto(explorationId, gaeParamChange) })
        .setClassifierModelId(checkElemPresent(gaeState.classifierModelId) {
          "Missing classifier_model_id in state $stateName in exploration $explorationId"
        })
        .setInteraction()
        .setSolicitAnswerDetails(checkElemPresent(gaeState.doesSolicitAnswerDetails) {
          "Missing solicit_answer_details in state $stateName in exploration $explorationId"
        })
        .build()
    }

    private fun convertGaeInteractionToProto(
      explorationId: String, stateName: String, gaeInteractionInstance: GaeInteractionInstance
    ): Interaction {
      return Interaction.newBuilder()
        .setId(checkElemPresent(gaeInteractionInstance.id) {
          "Missing interaction id in state $stateName in exploration $explorationId"
        })
        .addAllAnswerGroups()
        .setSolution()
        .addAllConfirmedUnclassifiedAnswers()
        .setHint()
        .setDefaultOutcome()
        .putAllCustomizationArgs()
        .build()
    }

    private fun convertGaeAnswerGroupToProto(
      explorationId: String, stateName: String, gaeAnswerGroup: GaeAnswerGroup
    ): AnswerGroup {
      // TODO(BenHenning): Add training data parsing.
      return AnswerGroup.newBuilder()
        .setTaggedSkillMisconceptionId(checkElemPresent(gaeAnswerGroup.taggedSkillMisconceptionId) {
          "Missing tagged_skill_misconception_id within state $stateName in exploration $explorationId"
        })
        .setOutcome(convertGaeOutcomeToProto(explorationId, stateName, checkElemPresent(gaeAnswerGroup.outcome) {
          "Missing outcome within state $stateName in exploration $explorationId"
        }))
        .addAllRuleSpecs()
        .build()
    }

    private fun convertGaeOutcomeToProto(explorationId: String, stateName: String, gaeOutcome: GaeOutcome): Outcome {
      return Outcome.newBuilder()
        .setDestStateName(checkElemPresent(gaeOutcome.dest) {
          "Missing dest in outcome within state $stateName in exploration $explorationId"
        })
        .setRefresherExplorationId(checkElemPresent(gaeOutcome.refresherExplorationId) {
          "Missing refresher_exploration_id in outcome within state $stateName in exploration $explorationId"
        })
        .setFeedback(convertGaeSubtitledHtmlToProto(checkElemPresent(gaeOutcome.feedback) {
          "Missing feedback in outcome within state $stateName in exploration $explorationId"
        }))
        .addAllParamChanges(checkElemPresent(gaeOutcome.paramChanges) {
          "Missing param_changes in outcome within state $stateName in exploration $explorationId"
        }.map { gaeParamChange -> convertGaeParamChangeToProto(explorationId, gaeParamChange) })
        .setMissingPrerequisiteSkillId(checkElemPresent(gaeOutcome.missingPrerequisiteSkillId) {
          "Missing missing_prerequisite_skill_id in outcome within state $stateName in exploration $explorationId"
        })
        .setLabelledAsCorrect(checkElemPresent(gaeOutcome.isLabelledAsCorrect) {
          "Missing labelled_as_correct in outcome within state $stateName in exploration $explorationId"
        })
        .build()
    }

    private fun convertGaeRuleSpecToProto(
      explorationId: String, stateName: String, gaeRuleSpec: GaeRuleSpec
    ): RuleSpec {
      return RuleSpec.newBuilder()

        .build()
    }

    private fun convertGaeParamChangeToProto(explorationId: String, gaeParamChange: GaeParamChange): ParamChange {
      return ParamChange.newBuilder()
        .setGeneratorId(checkElemPresent(gaeParamChange.generatorId) {
          "Expected generator_id for param change in exploration $explorationId"
        })
        .setName(checkElemPresent(gaeParamChange.name) {
          "Expected name for param change in exploration $explorationId"
        })
        .setCustomizationArgs(convertGaeCustomizationArgsToProto(
          explorationId, checkElemPresent(gaeParamChange.customizationArgs) {
            "Expected customization_args for param change in exploration $explorationId"
          })
        )
        .build()
    }

    private fun convertGaeCustomizationArgsToProto(
      explorationId: String, gaeCustomizationArgs: GaeCustomizationArgs
    ): ParamChangeCustomizationArgs {
      // TODO(BenHenning): Remove customization args that require Jinja since these won't work with Oppia Android.
      val gaeValue = checkElemPresent(gaeCustomizationArgs.value) {
        "Missing value in customization arguments within exploration $explorationId"
      }
      check(gaeValue is String) {
        "Expected value passed into param change customization arg to be type string for exploration $explorationId," +
            " but encountered: $gaeValue"
      }
      return ParamChangeCustomizationArgs
        .newBuilder()
        .setParseWithJinja(checkElemPresent(gaeCustomizationArgs.isParsedWithJinja) {
          "Missing parse_with_jinja in customization arguments within exploration $explorationId"
        })
        .addValue(gaeValue)
        .build()
    }

    private fun convertGaeParamSpecToProto(explorationId: String, gaeParamSpec: GaeParamSpec): ParamSpec {
      val objType = when(gaeParamSpec.objType) {
        "UnicodeString" -> ObjectType.UNICODE_STRING
        else -> throw IllegalStateException(
          "Unsupported object type: ${gaeParamSpec.objType} found in param spec within exploration $explorationId"
        )
      }
      return ParamSpec.newBuilder()
        .setObjType(objType)
        .build()
    }

    private fun generateTopicThumbnail(hash: Int): LessonThumbnail {
      val colors = listOf(0xff0000, 0x00ff00, 0x0000ff)
      return LessonThumbnail.newBuilder()
        .setBackgroundColorRgb(colors[hash % colors.size])
        .setThumbnailGraphic(LessonThumbnailGraphic.forNumber(hash % LessonThumbnailGraphic.values().size))
        .build()
    }

    private fun <T: Any?> checkElemPresent(value: T?, errorMessageComputer: () -> String): T {
      return value ?: throw MissingElementFromBackendException(errorMessageComputer())
    }
  }

  class MissingElementFromBackendException(reason: String): Exception(reason)
}

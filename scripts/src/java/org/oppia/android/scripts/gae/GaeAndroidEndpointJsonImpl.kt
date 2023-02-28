package org.oppia.android.scripts.gae

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.oppia.android.scripts.gae.compat.CompleteExploration
import org.oppia.android.scripts.gae.compat.CompleteTopicPack
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityConstraints
import org.oppia.android.scripts.gae.compat.TopicPackRepository
import org.oppia.android.scripts.gae.gcs.GcsService
import org.oppia.android.scripts.gae.json.AndroidActivityHandlerService
import org.oppia.android.scripts.gae.json.GaeClassroom
import org.oppia.android.scripts.gae.json.GaeSkill
import org.oppia.android.scripts.gae.json.GaeStory
import org.oppia.android.scripts.gae.json.GaeSubtopic
import org.oppia.android.scripts.gae.json.GaeSubtopicPage
import org.oppia.android.scripts.gae.json.GaeTopic
import org.oppia.android.scripts.gae.proto.ImageDownloader
import org.oppia.android.scripts.gae.proto.JsonToProtoConverter
import org.oppia.android.scripts.gae.proto.LocalizationTracker
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestConceptCardProtoVersion
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestExplorationProtoVersion
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestImageProtoVersion
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestLanguageProtosVersion
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestQuestionProtoVersion
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestRevisionCardProtoVersion
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestStateProtoVersion
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestTopicContentProtoVersion
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestTopicListProtoVersion
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider.createLatestTopicSummaryProtoVersion
import org.oppia.proto.v1.api.ClientCompatibilityContextDto
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.CONCEPT_CARD
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.CONCEPT_CARD_LANGUAGE_PACK
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.EXPLORATION
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.EXPLORATION_LANGUAGE_PACK
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.QUESTION
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.QUESTION_LANGUAGE_PACK
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.QUESTION_LIST_SKILL_ID
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.REVISION_CARD
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.REVISION_CARD_LANGUAGE_PACK
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.STRUCTURETYPE_NOT_SET
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase.TOPIC_SUMMARY_ID
import org.oppia.proto.v1.api.TopicContentRequestDto
import org.oppia.proto.v1.api.TopicContentResponseDto
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto
import org.oppia.proto.v1.api.TopicListRequestDto
import org.oppia.proto.v1.api.TopicListResponseDto
import org.oppia.proto.v1.api.TopicListResponseDto.DownloadableTopicDto
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.proto.v1.structure.SubtopicPageIdDto

class GaeAndroidEndpointJsonImpl(
  apiSecret: String,
  gaeBaseUrl: String,
  gcsBaseUrl: String,
  gcsBucket: String,
  private val coroutineDispatcher: CoroutineDispatcher,
  private val topicDependencies: Map<String, Set<String>>
) : GaeAndroidEndpoint {
  private val activityService by lazy { AndroidActivityHandlerService(apiSecret, gaeBaseUrl) }
  private val gcsService by lazy { GcsService(gcsBaseUrl, gcsBucket) }
  private val converterInitializer by lazy {
    ConverterInitializer(activityService, gcsService, coroutineDispatcher, topicDependencies)
  }
  private val contentCache by lazy { ContentCache() }

  override fun fetchTopicListAsync(request: TopicListRequestDto): Deferred<TopicListResponseDto> {
    return CoroutineScope(coroutineDispatcher).async {
      // First, verify the request proto version.
      check(request.protoVersion.version == createLatestTopicListProtoVersion().version) {
        "Unsupported request version encountered: ${request.protoVersion}."
      }

      // Second, verify the request compatibility (ignore existing downloads for local emulation).
      request.compatibilityContext.verifyCompatibility()

      // TODO: Add support for additional languages in summaries.
      val defaultLanguage = request.requestedDefaultLanguage
      val additionalLanguages = request.requiredAdditionalLanguagesList.toSet()
      val constraints =
        CompatibilityConstraints(
          supportedInteractionIds = SUPPORTED_INTERACTION_IDS,
          supportedDefaultLanguages = SUPPORTED_DEFAULT_LANGUAGES,
          requiredTranslationLanguages = additionalLanguages + defaultLanguage,
          supportedImageFormats = SUPPORTED_IMAGE_FORMATS,
          supportedAudioFormats = SUPPORTED_AUDIO_FORMATS,
          supportedHtmlTags = SUPPORTED_HTML_TAGS,
          supportedStateSchemaVersion = SUPPORTED_STATE_SCHEMA_VERSION,
          topicDependencies = topicDependencies
        )

      val jsonConverter = converterInitializer.getJsonToProtoConverter()
      val topicRepository = converterInitializer.getTopicPackRepository(constraints)

      val topicIds = fetchAllClassroomTopicIds()
      val availableTopicPacks = topicIds.map { topicId ->
        topicRepository.downloadConstructedCompleteTopicAsync(topicId)
      }.awaitAll().associateBy { it.topic.id }
      contentCache.addPacks(availableTopicPacks)
      jsonConverter.trackTopicTranslations(contentCache.topics)
      jsonConverter.trackStoryTranslations(contentCache.stories)
      jsonConverter.trackExplorationTranslations(contentCache.explorations)
      jsonConverter.trackConceptCardTranslations(contentCache.skills)
      jsonConverter.trackRevisionCardTranslations(contentCache.subtopics.values.toList())

      val missingTopicIds = topicIds - availableTopicPacks.keys
      val futureTopics = missingTopicIds.map { topicId ->
        activityService.fetchLatestTopicAsync(topicId)
      }.awaitAll().associateBy { it.id }
      jsonConverter.trackTopicTranslations(futureTopics)

      return@async TopicListResponseDto.newBuilder().apply {
        protoVersion = createLatestTopicListProtoVersion()
        addAllAvailableTopics(
          availableTopicPacks.map { (topicId, topicPack) ->
            TopicListResponseDto.AvailableTopicDto.newBuilder().apply {
              this.downloadableTopic = DownloadableTopicDto.newBuilder().apply {
                this.topicId = topicId
                this.topicSummary = jsonConverter.convertToDownloadableTopicSummary(
                  topicPack.topic,
                  defaultLanguage,
                  topicPack.subtopicPages,
                  topicPack.stories,
                  topicPack.explorations,
                  topicPack.referencedSkills
                )
                this.downloadSizeBytes = 0 // Not supported for local GAE endpoint emulation.
              }.build()
            }.build()
          }
        )
        addAllFutureTopics(
          futureTopics.map { (topicId, gaeTopic) ->
            TopicListResponseDto.FutureTopicDto.newBuilder().apply {
              this.topicId = topicId
              this.topicSummary =
                jsonConverter.convertToUpcomingTopicSummary(gaeTopic, defaultLanguage)
            }.build()
          }
        )
      }.build()
    }
  }

  override fun fetchTopicContentAsync(
    request: TopicContentRequestDto
  ): Deferred<TopicContentResponseDto> {
    return CoroutineScope(coroutineDispatcher).async {
      check(request.protoVersion.version <= createLatestTopicContentProtoVersion().version) {
        "Unsupported request version encountered: ${request.protoVersion}."
      }

      // Ignore the requested max payload size for local emulation. Proper error responses are also
      // not supported.
      TopicContentResponseDto.newBuilder().apply {
        protoVersion = createLatestTopicContentProtoVersion()
        addAllDownloadResults(request.identifiersList.map { fetchStructure(it) })
      }.build()
    }
  }

  private suspend fun fetchAllClassroomTopicIds(): List<String> {
    return CLASSROOMS.map(activityService::fetchLatestClassroomAsync)
      .awaitAll()
      .flatMap(GaeClassroom::topicIds)
      .distinct()
  }

  private suspend fun fetchStructure(
    identifier: DownloadRequestStructureIdentifierDto
  ): DownloadResultDto {
    return DownloadResultDto.newBuilder().apply {
      val fetcher = when (identifier.structureTypeCase) {
        REVISION_CARD -> StructureFetcher.RevisionCard
        CONCEPT_CARD -> StructureFetcher.ConceptCard
        EXPLORATION -> StructureFetcher.Exploration
        REVISION_CARD_LANGUAGE_PACK -> StructureFetcher.RevisionCardLanguagePack
        CONCEPT_CARD_LANGUAGE_PACK -> StructureFetcher.ConceptCardLanguagePack
        EXPLORATION_LANGUAGE_PACK -> StructureFetcher.ExplorationLanguagePack
        // Questions aren't yet available from Oppia web & the functionality is disabled in the app.
        // Also, topic summary isn't supported explicitly since it's receivable entirely through the
        // list request.
        TOPIC_SUMMARY_ID, QUESTION_LIST_SKILL_ID, QUESTION, QUESTION_LANGUAGE_PACK ->
          StructureFetcher.Unsupported
        STRUCTURETYPE_NOT_SET, null ->
          error("Encountered invalid request identifier: ${identifier.structureTypeCase}.")
      }

      this.identifier = identifier
      fetcher.fetchStructure(
        identifier,
        converterInitializer.getJsonToProtoConverter(),
        converterInitializer.getLocalizationTracker(),
        contentCache,
        resultBuilder = this
      )
    }.build()
  }

  private sealed class StructureFetcher {
    suspend fun fetchStructure(
      identifier: DownloadRequestStructureIdentifierDto,
      jsonConverter: JsonToProtoConverter,
      localizationTracker: LocalizationTracker,
      contentCache: ContentCache,
      resultBuilder: DownloadResultDto.Builder
    ) {
      resultBuilder.fetchAndSet(
        identifier, jsonConverter, localizationTracker, contentCache
      ).also { fetchedVersion ->
        check(fetchedVersion == identifier.contentVersion) {
          "Cannot fetch requested content version for: $identifier (fetched version:" +
            " $fetchedVersion)."
        }
      }
    }

    protected fun DownloadResultDto.Builder.setSkippedFromFailure(
      id: DownloadRequestStructureIdentifierDto
    ): Int {
      skippedFromFailure = true
      return id.contentVersion
    }

    protected abstract suspend fun DownloadResultDto.Builder.fetchAndSet(
      identifier: DownloadRequestStructureIdentifierDto,
      jsonConverter: JsonToProtoConverter,
      localizationTracker: LocalizationTracker,
      contentCache: ContentCache
    ): Int

    object RevisionCard : StructureFetcher() {
      override suspend fun DownloadResultDto.Builder.fetchAndSet(
        identifier: DownloadRequestStructureIdentifierDto,
        jsonConverter: JsonToProtoConverter,
        localizationTracker: LocalizationTracker,
        contentCache: ContentCache
      ): Int {
        val subtopicPageId = identifier.revisionCard.id
        val defaultLanguage = identifier.revisionCard.language
        val (subtopic, subtopicPage) = contentCache.subtopics.getValue(subtopicPageId)
        val containerId = LocalizationTracker.ContainerId.createFrom(subtopicPage, subtopic)
        return if (localizationTracker.isLanguageSupported(containerId, defaultLanguage)) {
          jsonConverter.convertToRevisionCard(subtopicPage, subtopic, defaultLanguage).also {
            this@fetchAndSet.revisionCard = it
          }.contentVersion
        } else setSkippedFromFailure(identifier)
      }
    }

    object ConceptCard : StructureFetcher() {
      override suspend fun DownloadResultDto.Builder.fetchAndSet(
        identifier: DownloadRequestStructureIdentifierDto,
        jsonConverter: JsonToProtoConverter,
        localizationTracker: LocalizationTracker,
        contentCache: ContentCache
      ): Int {
        val skillId = identifier.conceptCard.skillId
        val defaultLanguage = identifier.conceptCard.language
        val skill = contentCache.skills.getValue(skillId)
        val containerId = LocalizationTracker.ContainerId.createFrom(skill)
        return if (localizationTracker.isLanguageSupported(containerId, defaultLanguage)) {
          jsonConverter.convertToConceptCard(skill, defaultLanguage).also {
            this@fetchAndSet.conceptCard = it
          }.contentVersion
        } else setSkippedFromFailure(identifier)
      }
    }

    object Exploration : StructureFetcher() {
      override suspend fun DownloadResultDto.Builder.fetchAndSet(
        identifier: DownloadRequestStructureIdentifierDto,
        jsonConverter: JsonToProtoConverter,
        localizationTracker: LocalizationTracker,
        contentCache: ContentCache
      ): Int {
        val expId = identifier.exploration.explorationId
        val defaultLanguage = identifier.exploration.language
        val exploration = contentCache.explorations.getValue(expId).exploration
        val containerId = LocalizationTracker.ContainerId.createFrom(exploration)
        return if (localizationTracker.isLanguageSupported(containerId, defaultLanguage)) {
          jsonConverter.convertToExploration(exploration, defaultLanguage).also {
            this@fetchAndSet.exploration = it
          }.contentVersion
        } else setSkippedFromFailure(identifier)
      }
    }

    object RevisionCardLanguagePack : StructureFetcher() {
      override suspend fun DownloadResultDto.Builder.fetchAndSet(
        identifier: DownloadRequestStructureIdentifierDto,
        jsonConverter: JsonToProtoConverter,
        localizationTracker: LocalizationTracker,
        contentCache: ContentCache
      ): Int {
        val packId = identifier.revisionCardLanguagePack
        val (subtopic, subtopicPage) = contentCache.subtopics.getValue(packId.id)
        val containerId = LocalizationTracker.ContainerId.createFrom(subtopicPage, subtopic)
        return if (localizationTracker.isLanguageSupported(containerId, packId.language)) {
          jsonConverter.retrieveRevisionCardLanguagePack(packId, subtopic, subtopicPage).also {
            this@fetchAndSet.revisionCardLanguagePack = it
          }.contentVersion
        } else setSkippedFromFailure(identifier)
      }
    }

    object ConceptCardLanguagePack : StructureFetcher() {
      override suspend fun DownloadResultDto.Builder.fetchAndSet(
        identifier: DownloadRequestStructureIdentifierDto,
        jsonConverter: JsonToProtoConverter,
        localizationTracker: LocalizationTracker,
        contentCache: ContentCache
      ): Int {
        val packId = identifier.conceptCardLanguagePack
        val skill = contentCache.skills.getValue(packId.skillId)
        val containerId = LocalizationTracker.ContainerId.createFrom(skill)
        return if (localizationTracker.isLanguageSupported(containerId, packId.language)) {
          jsonConverter.retrieveConceptCardLanguagePack(packId, skill).also {
            this@fetchAndSet.conceptCardLanguagePack = it
          }.contentVersion
        } else setSkippedFromFailure(identifier)
      }
    }

    object ExplorationLanguagePack : StructureFetcher() {
      override suspend fun DownloadResultDto.Builder.fetchAndSet(
        identifier: DownloadRequestStructureIdentifierDto,
        jsonConverter: JsonToProtoConverter,
        localizationTracker: LocalizationTracker,
        contentCache: ContentCache
      ): Int {
        val packId = identifier.explorationLanguagePack
        val explorationId = packId.explorationId
        val requestedLanguage = packId.language
        val completedExploration = contentCache.explorations.getValue(explorationId)
        val containerId =
          LocalizationTracker.ContainerId.createFrom(completedExploration.exploration)
        return if (localizationTracker.isLanguageSupported(containerId, requestedLanguage)) {
          jsonConverter.convertToExplorationLanguagePack(
            packId, completedExploration.translations.getValue(requestedLanguage)
          ).also {
            this@fetchAndSet.explorationLanguagePack = it
          }.contentVersion
        } else setSkippedFromFailure(identifier)
      }
    }

    object Unsupported : StructureFetcher() {
      override suspend fun DownloadResultDto.Builder.fetchAndSet(
        identifier: DownloadRequestStructureIdentifierDto,
        jsonConverter: JsonToProtoConverter,
        localizationTracker: LocalizationTracker,
        contentCache: ContentCache
      ): Int = setSkippedFromFailure(identifier)
    }
  }

  private class ConverterInitializer(
    private val activityService: AndroidActivityHandlerService,
    private val gcsService: GcsService,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val topicDependencies: Map<String, Set<String>>
  ) {
    private var imageDownloader: ImageDownloader? = null
    private var localizationTracker: LocalizationTracker? = null
    private var jsonToProtoConverter: JsonToProtoConverter? = null
    private var topicPackRepositories =
      mutableMapOf<CompatibilityConstraints, TopicPackRepository>()

    fun getImageDownloader(): ImageDownloader = imageDownloader ?: initializeImageDownloader()

    suspend fun getLocalizationTracker(): LocalizationTracker =
      localizationTracker ?: initializeLocalizationTracker()

    suspend fun getJsonToProtoConverter(): JsonToProtoConverter =
      jsonToProtoConverter ?: initializeJsonToProtoConverter()

    suspend fun getTopicPackRepository(constraints: CompatibilityConstraints): TopicPackRepository =
      topicPackRepositories.getOrPut(constraints) { constructTopicPackRepository(constraints) }

    private fun initializeImageDownloader(): ImageDownloader {
      return ImageDownloader(gcsService, coroutineDispatcher).also {
        this.imageDownloader = it
      }
    }

    private suspend fun initializeLocalizationTracker(): LocalizationTracker {
      return LocalizationTracker.createTracker(getImageDownloader()).also {
        this.localizationTracker = it
      }
    }

    private suspend fun initializeJsonToProtoConverter(): JsonToProtoConverter {
      return JsonToProtoConverter(getLocalizationTracker(), topicDependencies).also {
        this.jsonToProtoConverter = it
      }
    }

    private suspend fun constructTopicPackRepository(
      constraints: CompatibilityConstraints
    ): TopicPackRepository {
      return TopicPackRepository(
        activityService, coroutineDispatcher, getLocalizationTracker(), constraints
      )
    }
  }

  private class ContentCache {
    private val internalTopicPackCache = mutableMapOf<String, CompleteTopicPack>()
    private val internalTopicsMap = mutableMapOf<String, GaeTopic>()
    private val internalStoriesMap = mutableMapOf<String, GaeStory>()
    private val internalSkillsMap = mutableMapOf<String, GaeSkill>()
    private val internalExplorationsMap = mutableMapOf<String, CompleteExploration>()
    private val internalSubtopicsMap =
      mutableMapOf<SubtopicPageIdDto, Pair<GaeSubtopic, GaeSubtopicPage>>()

    val topicPackCache: Map<String, CompleteTopicPack> = internalTopicPackCache
    val topics: Map<String, GaeTopic> = internalTopicsMap
    val stories: Map<String, GaeStory> = internalStoriesMap
    val skills: Map<String, GaeSkill> = internalSkillsMap
    val explorations: Map<String, CompleteExploration> = internalExplorationsMap
    val subtopics: Map<SubtopicPageIdDto, Pair<GaeSubtopic, GaeSubtopicPage>> = internalSubtopicsMap

    fun addPacks(packs: Map<String, CompleteTopicPack>) {
      internalTopicPackCache += packs
      recomputeIndexes()
    }

    private fun recomputeIndexes() {
      // Skills may be multi-referenced across topics, so just collect them all (rather than
      // specifically checking for non-duplicates; the repository is supposed to do that).
      topicPackCache.values.forEach { internalSkillsMap += it.referencedSkills }

      // Topics are globally unique and, per the topic pack structure, it cannot be duplicated.
      internalTopicsMap += topicPackCache.mapValues { (_, topicPack) -> topicPack.topic }

      // TODO: Maybe consolidate these & the explorations one when I can think more clearly.
      // Stories are globally unique.
      val allStories = topicPackCache.values.flatMap { it.stories.entries }
      val uniqueStories = allStories.groupBy { (storyId, _) ->
        storyId
      }.mapValues { (_, stories) -> stories.map { it.value }.single() }
      internalStoriesMap.clear()
      internalStoriesMap += uniqueStories.toMap()

      // Explorations should exist exactly once among all known topic packs (i.e. they shouldn't
      // belong to more than topic).
      val allExplorations = topicPackCache.values.flatMap { it.explorations.entries }
      val uniqueExplorations =
        allExplorations.groupBy { it.key }.mapValues { (_, exps) -> exps.map { it.value }.single() }
      internalExplorationsMap.clear()
      internalExplorationsMap += uniqueExplorations.toMap()

      // Subtopics are also globally unique.
      internalSubtopicsMap.clear()
      internalSubtopicsMap += topicPackCache.values.flatMap { topicPack ->
        topicPack.subtopicPages.entries.map { (subtopicPageId, subtopicPage) ->
          val subtopic = topicPack.topic.subtopics.find { subtopic ->
            subtopic.id == subtopicPageId.subtopicIndex
          } ?: error("Failed to find subtopic with ID: $subtopicPageId.")
          subtopicPageId to (subtopic to subtopicPage)
        }
      }
    }
  }

  private companion object {
    private val CLASSROOMS = setOf("math")

    private val SUPPORTED_INTERACTION_IDS =
      setOf(
        "Continue", "FractionInput", "ItemSelectionInput", "MultipleChoiceInput",
        "NumericInput", "TextInput", "DragAndDropSortInput", "ImageClickInput",
        "RatioExpressionInput", "EndExploration", "NumericExpressionInput",
        "AlgebraicExpressionInput", "MathEquationInput"
      )

    private val SUPPORTED_IMAGE_FORMATS = setOf("png", "webp", "svg", "svgz")

    private val SUPPORTED_AUDIO_FORMATS = setOf("mp3", "ogg")

    // Reference for HTML tags (Html.handleStartTag), though note some are removed if there are
    // Oppia versions that should be used, instead:
    // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java#784.
    private val ANDROID_SUPPORTED_HTML_TAGS = setOf(
      "br", "p", "ul", "li", "div", "span", "strong", "b", "em", "cite", "dfn", "i", "big", "small",
      "font", "blockquote", "tt", "a", "u", "del", "s", "strike", "sup", "sub", "h1", "h2", "h3",
      "h4", "h5", "h6"
    )

    private val SUPPORTED_OPPIA_HTML_TAGS = setOf(
      "oppia-noninteractive-image", "oppia-noninteractive-math", "oppia-noninteractive-skillreview"
    )

    private val SUPPORTED_HTML_TAGS = ANDROID_SUPPORTED_HTML_TAGS + SUPPORTED_OPPIA_HTML_TAGS

    // Only English is supported as an imported default language (to ensure that all English
    // translations are present which the app always expects).
    private val SUPPORTED_DEFAULT_LANGUAGES = setOf(LanguageType.ENGLISH)

    // From feconf.
    private const val SUPPORTED_STATE_SCHEMA_VERSION = 55

    private fun ClientCompatibilityContextDto.verifyCompatibility() {
      check(topicListRequestResponseProtoVersion == createLatestTopicListProtoVersion()) {
        "Unsupported topic list version: $topicListRequestResponseProtoVersion."
      }
      check(topicContentRequestResponseProtoVersion == createLatestTopicContentProtoVersion()) {
        "Unsupported topic content version: $topicContentRequestResponseProtoVersion."
      }
      check(topicSummaryProtoVersion == createLatestTopicSummaryProtoVersion()) {
        "Unsupported topic summary version: $topicSummaryProtoVersion."
      }
      check(revisionCardProtoVersion == createLatestRevisionCardProtoVersion()) {
        "Unsupported revision card version: $revisionCardProtoVersion."
      }
      check(conceptCardProtoVersion == createLatestConceptCardProtoVersion()) {
        "Unsupported revision card version: $conceptCardProtoVersion."
      }
      check(explorationProtoVersion == createLatestExplorationProtoVersion()) {
        "Unsupported revision card version: $explorationProtoVersion."
      }
      check(questionProtoVersion == createLatestQuestionProtoVersion()) {
        "Unsupported revision card version: $questionProtoVersion."
      }
      check(stateProtoVersion == createLatestStateProtoVersion()) {
        "Unsupported revision card version: $stateProtoVersion."
      }
      check(languageProtosVersion == createLatestLanguageProtosVersion()) {
        "Unsupported revision card version: $languageProtosVersion."
      }
      check(imageProtoVersion == createLatestImageProtoVersion()) {
        "Unsupported revision card version: $imageProtoVersion."
      }
    }
  }
}

package org.oppia.android.scripts.gae

import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import org.oppia.android.scripts.gae.compat.CompleteExploration
import org.oppia.android.scripts.gae.compat.CompleteTopicPack
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityConstraints
import org.oppia.android.scripts.gae.compat.TopicPackRepository
import org.oppia.android.scripts.gae.compat.TopicPackRepository.MetricCallbacks.DataGroupType
import org.oppia.android.scripts.gae.json.AndroidActivityHandlerService
import org.oppia.android.scripts.gae.json.GaeSkill
import org.oppia.android.scripts.gae.json.GaeStory
import org.oppia.android.scripts.gae.json.GaeSubtopic
import org.oppia.android.scripts.gae.json.GaeSubtopicPage
import org.oppia.android.scripts.gae.json.GaeTopic
import org.oppia.android.scripts.gae.proto.ImageDownloader
import org.oppia.android.scripts.gae.proto.JsonToProtoConverter
import org.oppia.android.scripts.gae.proto.LocalizationTracker
import org.oppia.android.scripts.gae.proto.LocalizationTracker.Companion.resolveLanguageCode
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
import org.oppia.android.scripts.proto.DownloadListVersions
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
  cacheDir: File?,
  forceCacheLoad: Boolean,
  private val coroutineDispatcher: CoroutineDispatcher,
  private val topicDependencies: Map<String, Set<String>>,
  private val imageDownloader: ImageDownloader,
  private val forcedVersions: DownloadListVersions?
) : GaeAndroidEndpoint {
  private val activityService by lazy {
    AndroidActivityHandlerService(
      apiSecret, gaeBaseUrl, cacheDir, forceCacheLoad, coroutineDispatcher
    )
  }
  private val converterInitializer by lazy {
    ConverterInitializer(
      activityService, coroutineDispatcher, topicDependencies, imageDownloader
    )
  }
  private val contentCache by lazy { ContentCache() }

  // TODO: Document that reportProgress's total can change over time (since it starts as an
  // estimate which might over-estimate).
  // TODO: It would be easier to track progress by using some sort of task management & collation
  //  system (which could also account for task weights and better control over estimation).
  override fun fetchTopicListAsync(
    request: TopicListRequestDto, reportProgress: (Int, Int) -> Unit
  ): Deferred<TopicListResponseDto> {
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
      val tracker =
        DownloadProgressTracker.createTracker(
          DownloadCountEstimator(classroomCount = SUPPORTED_CLASSROOMS.size),
          coroutineDispatcher,
          reportProgress
        )
      val constraints =
        CompatibilityConstraints(
          supportedInteractionIds = SUPPORTED_INTERACTION_IDS,
          supportedDefaultLanguages = SUPPORTED_DEFAULT_LANGUAGES,
          requiredTranslationLanguages = additionalLanguages + defaultLanguage,
          supportedImageFormats = SUPPORTED_IMAGE_FORMATS,
          supportedAudioFormats = SUPPORTED_AUDIO_FORMATS,
          supportedHtmlTags = SUPPORTED_HTML_TAGS,
          supportedStateSchemaVersion = SUPPORTED_STATE_SCHEMA_VERSION,
          topicDependencies = topicDependencies,
          forcedVersions = forcedVersions
        )

      val jsonConverter = converterInitializer.getJsonToProtoConverter()
      val topicRepository = converterInitializer.getTopicPackRepository(constraints)

      val topicIds = fetchAllClassroomTopicIdsAsync(tracker).await()
      val topicCountsTracker = TopicCountsTracker.createFrom(tracker, topicIds)

      val availableTopicPacks = topicIds.mapIndexed { index, topicId ->
        topicRepository.downloadConstructedCompleteTopicAsync(
          topicId, topicCountsTracker.topicStructureCountMap.getValue(topicId).metricsCallbacks
        ).also { tracker.reportDownloaded("${topicId}_$index") }
      }.awaitAll().associateBy { it.topic.id }

      val missingTopicIds = topicIds - availableTopicPacks.keys
      val futureTopics = missingTopicIds.map { topicId ->
        activityService.fetchLatestTopicAsync(topicId)
      }.awaitAll().associate { it.id to it.payload }

      contentCache.addPacks(availableTopicPacks)
      jsonConverter.trackTopicTranslations(contentCache.topics)
      jsonConverter.trackStoryTranslations(contentCache.stories)
      jsonConverter.trackExplorationTranslations(contentCache.explorations)
      jsonConverter.trackConceptCardTranslations(contentCache.skills)
      jsonConverter.trackRevisionCardTranslations(contentCache.subtopics.values.toList())
      jsonConverter.trackTopicTranslations(futureTopics)

      tracker.reportDownloaded("data_conversion_finished")
      tracker.reportDownloadsFinished()

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
    request: TopicContentRequestDto, reportProgress: (Int, Int) -> Unit
  ): Deferred<TopicContentResponseDto> {
    val progressChannel = Channel<Int>()
    progressChannel.consumeAsFlow().withIndex().onEach { (index, _) ->
      reportProgress(index + 1, request.identifiersCount)
    }.launchIn(CoroutineScope(coroutineDispatcher))

    return CoroutineScope(coroutineDispatcher).async {
      check(request.protoVersion.version <= createLatestTopicContentProtoVersion().version) {
        "Unsupported request version encountered: ${request.protoVersion}."
      }

      // Parallelize the structure assembly to better utilize multi-core machines.
      val delegatedScope = CoroutineScope(coroutineDispatcher)
      val results = request.identifiersList.mapIndexed { index, id ->
        delegatedScope.async {
          // Fetch the structure at this index and report when it's completed.
          fetchStructure(id).also { progressChannel.send(index) }
        }
      }

      // Ignore the requested max payload size for local emulation. Proper error responses are also
      // not supported.
      TopicContentResponseDto.newBuilder().apply {
        protoVersion = createLatestTopicContentProtoVersion()
        addAllDownloadResults(results.awaitAll().also { progressChannel.close() })
      }.build()
    }
  }

  private fun fetchAllClassroomTopicIdsAsync(
    tracker: DownloadProgressTracker
  ): Deferred<List<String>> {
    // TODO: Revert the temp change once all classrooms are supported in the new format.
    // TODO: Double check the language verification (since sWBXKH4PZcK6 Swahili isn't 100%).
    return CoroutineScope(coroutineDispatcher).async {
      listOf(
        "iX9kYCjnouWN", "sWBXKH4PZcK6", "C4fqwrvqWpRm", "qW12maD4hiA8", "0abdeaJhmfPm", "5g0nxGUmx5J5"
      ).also {
        tracker.countEstimator.setTopicCount(it.size)
        tracker.reportDownloaded("math")
      }
//       SUPPORTED_CLASSROOMS.map { classroomName ->
//         CoroutineScope(coroutineDispatcher).async {
//           activityService.fetchLatestClassroomAsync(classroomName).await().also {
//             tracker.reportDownloaded(classroomName)
//           }
//         }
//       }.awaitAll().flatMap(GaeClassroom::topicIds).distinct().also {
//         tracker.countEstimator.setTopicCount(it.size)
//       }
    }
  }

  private suspend fun fetchStructure(
    identifier: DownloadRequestStructureIdentifierDto
  ): DownloadResultDto {
    return DownloadResultDto.newBuilder().apply {
      val fetcher = when (identifier.structureTypeCase) {
        TOPIC_SUMMARY_ID -> StructureFetcher.TopicSummary
        REVISION_CARD -> StructureFetcher.RevisionCard
        CONCEPT_CARD -> StructureFetcher.ConceptCard
        EXPLORATION -> StructureFetcher.Exploration
        REVISION_CARD_LANGUAGE_PACK -> StructureFetcher.RevisionCardLanguagePack
        CONCEPT_CARD_LANGUAGE_PACK -> StructureFetcher.ConceptCardLanguagePack
        EXPLORATION_LANGUAGE_PACK -> StructureFetcher.ExplorationLanguagePack
        // Questions aren't yet available from Oppia web & the functionality is disabled in the app.
        QUESTION_LIST_SKILL_ID, QUESTION, QUESTION_LANGUAGE_PACK -> StructureFetcher.Unsupported
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

  private class TopicStructureCountTracker(
    private val topicId: String,
    private val notifyItemCountChanged: (DataGroupType) -> Unit,
    private val notifyItemDownloaded: suspend (String) -> Unit,
    private val currentNeededStoryCount: AtomicInteger = AtomicInteger(),
    private val currentNeededChapterCount: AtomicInteger = AtomicInteger(),
    private val currentNeededRevisionCardCount: AtomicInteger = AtomicInteger(),
    private val currentNeededConceptCardCount: AtomicInteger = AtomicInteger()
  ) {
    val metricsCallbacks by lazy {
      TopicPackRepository.MetricCallbacks(
        resetAllGroupItemCounts = this::resetAllItemCounts,
        resetGroupItemCount = this::resetGroupItemCount,
        reportGroupItemCount = this::reportGroupItemCount,
        reportGroupItemDownloaded = this::reportGroupItemDownloaded
      )
    }

    val neededStoryCount: Int get() = offsetStoryCount.get() + currentNeededStoryCount.get()
    val neededChapterCount: Int get() = offsetChapterCount.get() + currentNeededChapterCount.get()
    val neededRevisionCardCount: Int get() =
      offsetRevisionCardCount.get() + currentNeededRevisionCardCount.get()
    val neededConceptCardCount: Int get() =
      offsetConceptCardCount.get() + currentNeededConceptCardCount.get()

    private val offsetStoryCount = AtomicInteger()
    private val offsetChapterCount = AtomicInteger()
    private val offsetRevisionCardCount = AtomicInteger()
    private val offsetConceptCardCount = AtomicInteger()
    private val downloadedStoryCount = AtomicInteger()
    private val downloadedChapterCount = AtomicInteger()
    private val downloadedRevisionCardCount = AtomicInteger()
    private val downloadedConceptCardCount = AtomicInteger()
    
    private fun resetAllItemCounts() {
      DataGroupType.values().forEach(::resetGroupItemCount)
    }

    private fun resetGroupItemCount(dataGroupType: DataGroupType) {
      val currentNeededCount = when (dataGroupType) {
        DataGroupType.STORY -> currentNeededStoryCount
        DataGroupType.SUBTOPIC -> currentNeededRevisionCardCount
        DataGroupType.EXPLORATION -> currentNeededChapterCount
        DataGroupType.SKILL -> currentNeededConceptCardCount
      }
      val downloadedCount = when (dataGroupType) {
        DataGroupType.STORY -> downloadedStoryCount
        DataGroupType.SUBTOPIC -> downloadedRevisionCardCount
        DataGroupType.EXPLORATION -> downloadedChapterCount
        DataGroupType.SKILL -> downloadedConceptCardCount
      }
      val offsetCount = when (dataGroupType) {
        DataGroupType.STORY -> offsetStoryCount
        DataGroupType.SUBTOPIC -> offsetRevisionCardCount
        DataGroupType.EXPLORATION -> offsetChapterCount
        DataGroupType.SKILL -> offsetConceptCardCount
      }

      // Reset means a whole new list of items will be reported. However, since previous items were
      // already reported, keep track of how many there were so that the counts don't become off.
      currentNeededCount.set(0)
      offsetCount.addAndGet(downloadedCount.getAndSet(0))
    }

    private fun reportGroupItemCount(dataGroupType: DataGroupType, count: Int) {
      val atomicToUpdate = when (dataGroupType) {
        DataGroupType.STORY -> currentNeededStoryCount
        DataGroupType.SUBTOPIC -> currentNeededRevisionCardCount
        DataGroupType.EXPLORATION -> currentNeededChapterCount
        DataGroupType.SKILL -> currentNeededConceptCardCount
      }
      atomicToUpdate.set(count)
      notifyItemCountChanged(dataGroupType)
    }

    private suspend fun reportGroupItemDownloaded(dataGroupType: DataGroupType, itemId: String) {
      val neededCount = when (dataGroupType) {
        DataGroupType.STORY -> currentNeededStoryCount.get()
        DataGroupType.SUBTOPIC -> currentNeededRevisionCardCount.get()
        DataGroupType.EXPLORATION -> currentNeededChapterCount.get()
        DataGroupType.SKILL -> currentNeededConceptCardCount.get()
      }
      val atomicToUpdate = when (dataGroupType) {
        DataGroupType.STORY -> downloadedStoryCount
        DataGroupType.SUBTOPIC -> downloadedRevisionCardCount
        DataGroupType.EXPLORATION -> downloadedChapterCount
        DataGroupType.SKILL -> downloadedConceptCardCount
      }
      atomicToUpdate.incrementAndGet()

      // Ensure the item is unique by prefixing it both with the topic and with current expected
      // count of the item category (in case it gets reported again later).
      notifyItemDownloaded("$topicId-$itemId-of-$neededCount")
    }
  }

  private class TopicCountsTracker private constructor(
    private val downloadProgressTracker: DownloadProgressTracker,
    private val topicIds: List<String>
  ) {
    val topicStructureCountMap by lazy {
      topicIds.associateWith {
        TopicStructureCountTracker(
          it,
          notifyItemCountChanged = this::notifyItemCountChanged,
          notifyItemDownloaded = this::notifyItemDownloaded
        )
      }
    }

    private val totalStoryCount: Int get() =
      topicStructureCountMap.values.sumOf { it.neededStoryCount }
    private val totalChapterCount: Int get() =
      topicStructureCountMap.values.sumOf { it.neededChapterCount }
    private val totalRevisionCardCount: Int get() =
      topicStructureCountMap.values.sumOf { it.neededRevisionCardCount }
    private val totalConceptCardCount: Int get() =
      topicStructureCountMap.values.sumOf { it.neededConceptCardCount }

    private fun notifyItemCountChanged(dataGroupType: DataGroupType) {
      when (dataGroupType) {
        DataGroupType.STORY -> downloadProgressTracker.countEstimator.setStoryCount(totalStoryCount)
        DataGroupType.SUBTOPIC ->
          downloadProgressTracker.countEstimator.setSubtopicCount(totalRevisionCardCount)
        DataGroupType.EXPLORATION ->
          downloadProgressTracker.countEstimator.setChapterCount(totalChapterCount)
        DataGroupType.SKILL ->
          downloadProgressTracker.countEstimator.setSkillCount(totalConceptCardCount)
      }
    }

    private suspend fun notifyItemDownloaded(uniqueItemId: String) =
      downloadProgressTracker.reportDownloaded(uniqueItemId)

    companion object {
      fun createFrom(
        downloadProgressTracker: DownloadProgressTracker, topicIds: List<String>
      ): TopicCountsTracker = TopicCountsTracker(downloadProgressTracker, topicIds)
    }
  }

  // TODO: Document that this tracker isn't estimating or tracking multiple versions (versions
  //  should be consolidated such that all versions for a particular ID needs to be resolved for
  //  that 'ID' to be done).
  private class DownloadProgressTracker private constructor(
    val countEstimator: DownloadCountEstimator,
    private val channel: SendChannel<String>
  ) {
    suspend fun reportDownloaded(contentGuid: String) = channel.send(contentGuid)

    fun reportDownloadsFinished() = channel.close()

    companion object {
      fun createTracker(
        countEstimator: DownloadCountEstimator,
        coroutineDispatcher: CoroutineDispatcher,
        reportProgress: (Int, Int) -> Unit
      ): DownloadProgressTracker {
        val progressChannel = Channel<String>().also {
          it.consumeAsFlow().withIndex().onEach { (index, _) ->
            // Note the extra '+1' for the download count is to account for data conversion.
            reportProgress(index + 1, countEstimator.estimatedDownloadCount + 1)
          }.launchIn(CoroutineScope(coroutineDispatcher))
        }
        return DownloadProgressTracker(countEstimator, progressChannel)
      }
    }
  }

  private class DownloadCountEstimator(classroomCount: Int) {
    // TODO: Add actual computations.
    private val classroomCount by lazy { MetricEstimator.Constant(classroomCount) }
    private val topicCount by lazy {
      MetricEstimator.Derived(ESTIMATED_AVERAGE_TOPICS_PER_CLASSROOM, this.classroomCount)
    }
    private val storyCount by lazy {
      MetricEstimator.Derived(ESTIMATED_AVERAGE_STORIES_PER_TOPIC, topicCount)
    }
    private val chapterCount by lazy {
      MetricEstimator.Derived(ESTIMATED_AVERAGE_CHAPTERS_PER_STORY, storyCount)
    }
    private val subtopicCount by lazy {
      MetricEstimator.Derived(ESTIMATED_AVERAGE_SUBTOPICS_PER_TOPIC, topicCount)
    }
    private val revisionCardCount by lazy { MetricEstimator.Alias(subtopicCount) }
    private val skillCount by lazy {
      MetricEstimator.Derived(ESTIMATED_AVERAGE_SKILLS_PER_SUBTOPIC, subtopicCount)
    }
    private val conceptCardCount by lazy { MetricEstimator.Alias(skillCount) }

    private val completeContentCount by lazy {
      MetricEstimator.Aggregate(
        listOf(
          this.classroomCount, topicCount, storyCount, chapterCount, revisionCardCount,
          conceptCardCount
        )
      )
    }

    val estimatedDownloadCount: Int get() = completeContentCount.currentValue

    fun setTopicCount(count: Int) = topicCount.setActualCount(count)
    fun setStoryCount(count: Int) = storyCount.setActualCount(count)
    fun setChapterCount(count: Int) = chapterCount.setActualCount(count)
    fun setSubtopicCount(count: Int) = subtopicCount.setActualCount(count)
    fun setSkillCount(count: Int) = skillCount.setActualCount(count)

    private sealed class MetricEstimator {
      abstract val currentValue: Int

      data class Constant(override val currentValue: Int): MetricEstimator()

      data class Derived(val estimatedRate: Int, val base: MetricEstimator): MetricEstimator() {
        private var actualCount: Int? = null
        override val currentValue: Int get() = actualCount ?: (estimatedRate * base.currentValue)

        fun setActualCount(actualCount: Int) {
          this.actualCount = actualCount
        }
      }

      data class Alias(val delegate: MetricEstimator): MetricEstimator() {
        override val currentValue: Int get() = delegate.currentValue
      }

      data class Aggregate(val metrics: List<MetricEstimator>): MetricEstimator() {
        override val currentValue: Int get() = metrics.sumOf(MetricEstimator::currentValue)
      }
    }

    private companion object {
      private const val ESTIMATED_AVERAGE_TOPICS_PER_CLASSROOM = 10
      private const val ESTIMATED_AVERAGE_STORIES_PER_TOPIC = 1
      private const val ESTIMATED_AVERAGE_CHAPTERS_PER_STORY = 10
      private const val ESTIMATED_AVERAGE_SUBTOPICS_PER_TOPIC = 10
      private const val ESTIMATED_AVERAGE_SKILLS_PER_SUBTOPIC = 3
    }
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

    object TopicSummary : StructureFetcher() {
      override suspend fun DownloadResultDto.Builder.fetchAndSet(
        identifier: DownloadRequestStructureIdentifierDto,
        jsonConverter: JsonToProtoConverter,
        localizationTracker: LocalizationTracker,
        contentCache: ContentCache
      ): Int {
        val topic = contentCache.topics.getValue(identifier.topicSummaryId)
        val containerId = LocalizationTracker.ContainerId.createFrom(topic)
        val defaultLanguage = topic.languageCode.resolveLanguageCode()
        val subtopicIds = topic.subtopics.map { subtopic ->
          SubtopicPageIdDto.newBuilder().apply {
            this.topicId = topic.id
            this.subtopicIndex = subtopic.id
          }.build()
        }

        val storyIds = topic.computeReferencedStoryIds()
        val subtopicPages = subtopicIds.associateWith { contentCache.subtopics.getValue(it).second }
        val stories = storyIds.associateWith { contentCache.stories.getValue(it) }
        val expIds = stories.values.flatMap { it.computeReferencedExplorationIds() }
        val explorations = expIds.associateWith { contentCache.explorations.getValue(it) }

        val skillIds = topic.computeDirectlyReferencedSkillIds() +
          stories.values.flatMap { it.computeDirectlyReferencedSkillIds() }
        val referencedSkills = skillIds.associateWith { contentCache.skills.getValue(it) }

        return if (localizationTracker.isLanguageSupported(containerId, defaultLanguage)) {
          jsonConverter.convertToDownloadableTopicSummary(
            topic, defaultLanguage, subtopicPages, stories, explorations, referencedSkills
          ).also { this@fetchAndSet.topicSummary = it }.contentVersion
        } else setSkippedFromFailure(identifier)
      }
    }

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
            packId, completedExploration.translations.getValue(requestedLanguage).expectedVersion
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
    private val coroutineDispatcher: CoroutineDispatcher,
    private val topicDependencies: Map<String, Set<String>>,
    private val imageDownloader: ImageDownloader
  ) {
    private var localizationTracker: LocalizationTracker? = null
    private var jsonToProtoConverter: JsonToProtoConverter? = null
    private var topicPackRepositories =
      mutableMapOf<CompatibilityConstraints, TopicPackRepository>()

    suspend fun getLocalizationTracker(): LocalizationTracker =
      localizationTracker ?: initializeLocalizationTracker()

    suspend fun getJsonToProtoConverter(): JsonToProtoConverter =
      jsonToProtoConverter ?: initializeJsonToProtoConverter()

    suspend fun getTopicPackRepository(constraints: CompatibilityConstraints): TopicPackRepository =
      topicPackRepositories.getOrPut(constraints) { constructTopicPackRepository(constraints) }

    private suspend fun initializeLocalizationTracker(): LocalizationTracker =
      LocalizationTracker.createTracker(imageDownloader).also { this.localizationTracker = it }

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
    private val SUPPORTED_CLASSROOMS = setOf("math")

    private val SUPPORTED_INTERACTION_IDS =
      setOf(
        "Continue", "FractionInput", "ItemSelectionInput", "MultipleChoiceInput",
        "NumericInput", "TextInput", "DragAndDropSortInput", "ImageClickInput",
        "RatioExpressionInput", "EndExploration", "NumericExpressionInput",
        "AlgebraicExpressionInput", "MathEquationInput"
      )

    // TODO: Remove gif.
    private val SUPPORTED_IMAGE_FORMATS = setOf("png", "webp", "svg", "svgz", "gif")

    private val SUPPORTED_AUDIO_FORMATS = setOf("mp3", "ogg")

    // Reference for HTML tags (Html.handleStartTag), though note some are removed if there are
    // Oppia versions that should be used, instead:
    // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java#784.
    private val ANDROID_SUPPORTED_HTML_TAGS = setOf(
      "br", "p", "ul", "ol", "li", "div", "span", "strong", "b", "em", "cite", "dfn", "i", "big",
      "small", "font", "blockquote", "tt", "a", "u", "del", "s", "strike", "sup", "sub", "h1", "h2",
      "h3", "h4", "h5", "h6", "pre"
    )

    private val SUPPORTED_OPPIA_HTML_TAGS = setOf(
      "oppia-noninteractive-image",
      "oppia-noninteractive-math",
      "oppia-noninteractive-skillreview",
      "oppia-noninteractive-link", // TODO: This shouldn't be present.
      "oppia-noninteractive-tabs", // TODO: This shouldn't be present.
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

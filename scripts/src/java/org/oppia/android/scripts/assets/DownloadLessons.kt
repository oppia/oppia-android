package org.oppia.android.scripts.assets

import com.google.protobuf.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.oppia.android.scripts.gae.GaeAndroidEndpoint
import org.oppia.android.scripts.gae.GaeAndroidEndpointJsonImpl
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider
import org.oppia.proto.v1.api.AndroidClientContextDto
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto
import org.oppia.proto.v1.api.TopicContentRequestDto
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.SKIPPED_FROM_FAILURE
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.SKIPPED_SHOULD_RETRY
import org.oppia.proto.v1.api.TopicListRequestDto
import org.oppia.proto.v1.api.TopicListResponseDto.AvailableTopicDto.AvailabilityTypeCase.DOWNLOADABLE_TOPIC
import org.oppia.proto.v1.structure.DownloadableTopicSummaryDto
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.proto.v1.structure.LocalizedConceptCardIdDto
import org.oppia.proto.v1.structure.LocalizedExplorationIdDto
import org.oppia.proto.v1.structure.LocalizedRevisionCardIdDto
import org.oppia.proto.v1.structure.SubtopicPageIdDto
import org.oppia.proto.v1.structure.SubtopicSummaryDto
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.Builder as DownloadReqStructIdDtoBuilder

// TODO: hook up to language configs for prod/dev language restrictions.
// TODO: Consider using better argument parser so that dev env vals can be defaulted.
fun main(vararg args: String) {
  check(args.size >= 4) {
    "Expected use: bazel run //scripts:download_lessons <base_url> <gcs_base_url> <gcs_bucket>" +
      " <api_secret> [test,topic,ids]"
  }
  val (baseUrl, gcsBaseUrl, gcsBucket, apiSecret) = args
  val testTopicIds = args.getOrNull(4)?.split(',')?.toSet() ?: setOf()
  DownloadLessons(baseUrl, gcsBaseUrl, gcsBucket, apiSecret, testTopicIds).downloadLessons()
}

class DownloadLessons(
  gaeBaseUrl: String,
  gcsBaseUrl: String,
  gcsBucket: String,
  apiSecret: String,
  testTopicIds: Set<String>
) {
  private val threadPool by lazy { Executors.newCachedThreadPool() }
  private val coroutineDispatcher by lazy { threadPool.asCoroutineDispatcher() }
  private val androidEndpoint: GaeAndroidEndpoint by lazy {
    GaeAndroidEndpointJsonImpl(
      apiSecret,
      gaeBaseUrl,
      gcsBaseUrl,
      gcsBucket,
      coroutineDispatcher,
      topicDependencies = topicDependenciesTable + testTopicIds.associateWith { setOf() }
    )
  }

  fun downloadLessons() {
    // TODO: Add destination (for writing, and maybe caching check?)

    val downloadJob = CoroutineScope(coroutineDispatcher).launch { downloadAllLessons() }
    runBlocking {
      try {
        downloadJob.join()
      } finally {
        shutdownBlocking()
      }
    }
  }

  private suspend fun downloadAllLessons() {
    val defaultLanguage = LanguageType.ENGLISH
    val supportedLanguages =
      LanguageType.values().filterNot { it in INVALID_LANGUAGE_TYPES || it == defaultLanguage }
    val listRequest = TopicListRequestDto.newBuilder().apply {
      protoVersion = ProtoVersionProvider.createLatestTopicListProtoVersion()
      clientContext = CLIENT_CONTEXT
      compatibilityContext = ProtoVersionProvider.createCompatibilityContext()
      // No structures are considered already downloaded. TODO: Integrate with local files cache?
      requestedDefaultLanguage = defaultLanguage
      addAllSupportedAdditionalLanguages(supportedLanguages)
    }.build()

    println("Sending topic list download request:\n$listRequest.")
    val listResponse = androidEndpoint.fetchTopicListAsync(listRequest).await()
    val downloadableTopics = listResponse.availableTopicsList.filter { availableTopic ->
      availableTopic.availabilityTypeCase == DOWNLOADABLE_TOPIC
    }.map { it.downloadableTopic.topicSummary }
    val downloadableTopicIds = downloadableTopics.map { it.id }
    val futureTopicIds = listResponse.futureTopicsList.map { it.topicId }
    println(
      "Downloaded topic results: ${listResponse.availableTopicsCount} topics are available," +
        " ${downloadableTopics.size} are downloadable, IDs: $downloadableTopicIds." +
        " ${futureTopicIds.size} topics will later be available, IDs: $futureTopicIds."
    )

    val contentRequest =
      createDownloadContentRequest(downloadableTopics, defaultLanguage, supportedLanguages)
    println("Requesting to download ${contentRequest.identifiersCount} content items...")
    val contentResponse = androidEndpoint.fetchTopicContentAsync(contentRequest).await()

    val successfulResults = contentResponse.downloadResultsList.filter {
      it.resultTypeCase != SKIPPED_FROM_FAILURE && it.resultTypeCase != SKIPPED_SHOULD_RETRY
    }
    println(
      "Received content response with ${contentResponse.downloadResultsCount} results," +
        " ${successfulResults.size} succeeded. Successes:" +
        "\n${successfulResults.map { it.resultTypeCase }}"
    )
  }

  private fun createDownloadContentRequest(
    topicSummaries: List<DownloadableTopicSummaryDto>,
    defaultLanguage: LanguageType,
    requestedLanguages: List<LanguageType>
  ): TopicContentRequestDto {
    return TopicContentRequestDto.newBuilder().apply {
      val allIdentifiers = topicSummaries.flatMap { topicSummary ->
        generateIdentifiersToDownloadTopic(topicSummary, defaultLanguage, requestedLanguages)
      }

      protoVersion = ProtoVersionProvider.createLatestTopicContentProtoVersion()
      clientContext = CLIENT_CONTEXT
      addAllIdentifiers(allIdentifiers.distinct())
      requestedMaxPayloadSizeBytes = 0 // This isn't used for local emulation.
    }.build()
  }

  private fun generateIdentifiersToDownloadTopic(
    topicSummary: DownloadableTopicSummaryDto,
    defaultLanguage: LanguageType,
    requestedLanguages: List<LanguageType>
  ): List<DownloadRequestStructureIdentifierDto> {
    return generateIdentifiersToDownloadRevisionCards(
      topicSummary.id,
      topicSummary.subtopicSummariesList,
      defaultLanguage,
      requestedLanguages
    ) + generateIdentifiersToDownloadExplorations(
      topicSummary, defaultLanguage, requestedLanguages
    ) + generateIdentifiersToDownloadConceptCards(topicSummary, defaultLanguage, requestedLanguages)
  }

  private fun generateIdentifiersToDownloadRevisionCards(
    topicId: String,
    subtopicSummaries: List<SubtopicSummaryDto>,
    defaultLanguage: LanguageType,
    requestedLanguages: List<LanguageType>
  ): List<DownloadRequestStructureIdentifierDto> {
    return subtopicSummaries.associateBy { subtopicSummary ->
      createSubtopicId(topicId, subtopicSummary.index)
    }.flatMap { (subtopicId, subtopicSummary) ->
      generateIdentifiersToDownloadStructure(
        subtopicId,
        subtopicSummary.contentVersion,
        defaultLanguage,
        requestedLanguages,
        ::createLocalizedRevisionCardId,
        setIdForStruct = DownloadReqStructIdDtoBuilder::setRevisionCard,
        setIdForLangPack = DownloadReqStructIdDtoBuilder::setRevisionCardLanguagePack
      )
    }
  }

  private fun generateIdentifiersToDownloadExplorations(
    downloadableTopicSummary: DownloadableTopicSummaryDto,
    defaultLanguage: LanguageType,
    requestedLanguages: List<LanguageType>
  ): List<DownloadRequestStructureIdentifierDto> {
    return downloadableTopicSummary.storySummariesList.flatMap { storySummary ->
      storySummary.chaptersList
    }.associateBy { chapterSummary ->
      chapterSummary.explorationId
    }.flatMap { (explorationId, chapterSummary) ->
      generateIdentifiersToDownloadStructure(
        explorationId,
        chapterSummary.contentVersion,
        defaultLanguage,
        requestedLanguages,
        ::createLocalizedExplorationId,
        setIdForStruct = DownloadReqStructIdDtoBuilder::setExploration,
        setIdForLangPack = DownloadReqStructIdDtoBuilder::setExplorationLanguagePack
      )
    }
  }

  private fun generateIdentifiersToDownloadConceptCards(
    downloadableTopicSummary: DownloadableTopicSummaryDto,
    defaultLanguage: LanguageType,
    requestedLanguages: List<LanguageType>
  ): List<DownloadRequestStructureIdentifierDto> {
    return downloadableTopicSummary.referencedSkillsList.associateBy { skillSummary ->
      skillSummary.id
    }.flatMap { (skillId, skillSummary) ->
      generateIdentifiersToDownloadStructure(
        skillId,
        skillSummary.contentVersion,
        defaultLanguage,
        requestedLanguages,
        ::createLocalizedConceptCardId,
        setIdForStruct = DownloadReqStructIdDtoBuilder::setConceptCard,
        setIdForLangPack = DownloadReqStructIdDtoBuilder::setConceptCardLanguagePack
      )
    }
  }

  private fun <I, L : Message> generateIdentifiersToDownloadStructure(
    id: I,
    contentVersion: Int,
    defaultLanguage: LanguageType,
    requestedLanguages: List<LanguageType>,
    createLocalizedId: (I, LanguageType) -> L,
    setIdForStruct: DownloadReqStructIdDtoBuilder.(L) -> DownloadReqStructIdDtoBuilder,
    setIdForLangPack: DownloadReqStructIdDtoBuilder.(L) -> DownloadReqStructIdDtoBuilder
  ): List<DownloadRequestStructureIdentifierDto> {
    return requestedLanguages.map { language ->
      createLocalizedId(id, language).toStructureIdentifier(contentVersion, setIdForLangPack)
    } + createLocalizedId(id, defaultLanguage).toStructureIdentifier(contentVersion, setIdForStruct)
  }

  private fun shutdownBlocking() {
    coroutineDispatcher.close()
    threadPool.tryShutdownFully(timeout = 5, unit = TimeUnit.SECONDS)
  }

  private companion object {
    private val INVALID_LANGUAGE_TYPES =
      listOf(LanguageType.LANGUAGE_CODE_UNSPECIFIED, LanguageType.UNRECOGNIZED)
    private val CLIENT_CONTEXT = AndroidClientContextDto.newBuilder().apply {
      appVersionName = checkNotNull(DownloadLessons::class.qualifiedName)
      appVersionCode = 0
    }.build()

    private fun ExecutorService.tryShutdownFully(timeout: Long, unit: TimeUnit) {
      // Try to fully shutdown the executor service per https://stackoverflow.com/a/33690603 and
      // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html.
      shutdown()
      try {
        if (!awaitTermination(timeout, unit)) {
          shutdownNow()
          check(awaitTermination(timeout, unit)) {
            "Executor service didn't fully shutdown: $this."
          }
        }
      } catch (e: InterruptedException) {
        shutdownNow()
        Thread.currentThread().interrupt()
      }
    }

    private const val PLACE_VALUES_ID = "iX9kYCjnouWN"
    private const val ADDITION_AND_SUBTRACTION_ID = "sWBXKH4PZcK6"
    private const val MULTIPLICATION_ID = "C4fqwrvqWpRm"
    private const val DIVISION_ID = "qW12maD4hiA8"
    private const val EXPRESSIONS_AND_EQUATIONS_ID = "dLmjjMDbCcrf"
    private const val FRACTIONS_ID = "0abdeaJhmfPm"
    private const val RATIOS_ID = "5g0nxGUmx5J5"

    private val fractionsDependencies by lazy {
      setOf(ADDITION_AND_SUBTRACTION_ID, MULTIPLICATION_ID, DIVISION_ID)
    }
    private val ratiosDependencies by lazy {
      setOf(ADDITION_AND_SUBTRACTION_ID, MULTIPLICATION_ID, DIVISION_ID)
    }
    private val additionAndSubtractionDependencies by lazy { setOf(PLACE_VALUES_ID) }
    private val multiplicationDependencies by lazy { setOf(ADDITION_AND_SUBTRACTION_ID) }
    private val divisionDependencies by lazy { setOf(MULTIPLICATION_ID) }
    private val placeValuesDependencies by lazy { setOf<String>() }
    private val expressionsAndEquationsDependencies by lazy {
      setOf(ADDITION_AND_SUBTRACTION_ID, MULTIPLICATION_ID, DIVISION_ID)
    }

    // TODO: Document that this exists since Oppia web doesn't yet provide signals on order.
    private val topicDependenciesTable by lazy {
      mapOf(
        FRACTIONS_ID to fractionsDependencies,
        RATIOS_ID to ratiosDependencies,
        ADDITION_AND_SUBTRACTION_ID to additionAndSubtractionDependencies,
        MULTIPLICATION_ID to multiplicationDependencies,
        DIVISION_ID to divisionDependencies,
        PLACE_VALUES_ID to placeValuesDependencies,
        EXPRESSIONS_AND_EQUATIONS_ID to expressionsAndEquationsDependencies,
      )
    }

    private fun createSubtopicId(topicId: String, subtopicIndex: Int): SubtopicPageIdDto {
      return SubtopicPageIdDto.newBuilder().apply {
        this.topicId = topicId
        this.subtopicIndex = subtopicIndex
      }.build()
    }

    private fun createLocalizedRevisionCardId(
      id: SubtopicPageIdDto,
      language: LanguageType
    ): LocalizedRevisionCardIdDto {
      return LocalizedRevisionCardIdDto.newBuilder().apply {
        this.id = id
        this.language = language
      }.build()
    }

    private fun createLocalizedExplorationId(
      explorationId: String,
      language: LanguageType
    ): LocalizedExplorationIdDto {
      return LocalizedExplorationIdDto.newBuilder().apply {
        this.explorationId = explorationId
        this.language = language
      }.build()
    }

    private fun createLocalizedConceptCardId(
      skillId: String,
      language: LanguageType
    ): LocalizedConceptCardIdDto {
      return LocalizedConceptCardIdDto.newBuilder().apply {
        this.skillId = skillId
        this.language = language
      }.build()
    }

    private fun <T : Message> T.toStructureIdentifier(
      contentVersion: Int,
      setValue: DownloadReqStructIdDtoBuilder.(T) -> DownloadReqStructIdDtoBuilder
    ): DownloadRequestStructureIdentifierDto {
      return DownloadRequestStructureIdentifierDto.newBuilder().apply {
        this.contentVersion = contentVersion
        this.setValue(this@toStructureIdentifier)
      }.build()
    }
  }
}

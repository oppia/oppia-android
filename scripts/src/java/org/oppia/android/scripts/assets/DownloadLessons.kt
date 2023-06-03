package org.oppia.android.scripts.assets

import com.google.protobuf.Message
import com.google.protobuf.TextFormat
import java.io.File
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.withContext
import org.oppia.android.scripts.gae.gcs.GcsService
import org.oppia.android.scripts.gae.gcs.GcsService.EntityType
import org.oppia.android.scripts.gae.gcs.GcsService.ImageType
import org.oppia.android.scripts.gae.proto.ImageDownloader
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.Builder as DownloadReqStructIdDtoBuilder
import org.oppia.proto.v1.api.TopicContentResponseDto
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.CONCEPT_CARD
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.CONCEPT_CARD_LANGUAGE_PACK
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.EXPLORATION
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.EXPLORATION_LANGUAGE_PACK
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.QUESTION
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.QUESTION_ID_LIST
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.QUESTION_LANGUAGE_PACK
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.RESULTTYPE_NOT_SET
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.REVISION_CARD
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.REVISION_CARD_LANGUAGE_PACK
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.TOPIC_SUMMARY
import org.oppia.proto.v1.structure.ChapterSummaryDto
import org.oppia.proto.v1.structure.ConceptCardDto
import org.oppia.proto.v1.structure.ConceptCardLanguagePackDto
import org.oppia.proto.v1.structure.ContentLocalizationDto
import org.oppia.proto.v1.structure.ContentLocalizationsDto
import org.oppia.proto.v1.structure.ExplorationDto
import org.oppia.proto.v1.structure.ExplorationLanguagePackDto
import org.oppia.proto.v1.structure.QuestionDto
import org.oppia.proto.v1.structure.QuestionLanguagePackDto
import org.oppia.proto.v1.structure.ReferencedImageDto
import org.oppia.proto.v1.structure.ReferencedImageListDto
import org.oppia.proto.v1.structure.RevisionCardDto
import org.oppia.proto.v1.structure.RevisionCardLanguagePackDto
import org.oppia.proto.v1.structure.SkillSummaryDto
import org.oppia.proto.v1.structure.StorySummaryDto
import org.oppia.proto.v1.structure.ThumbnailDto

// TODO: hook up to language configs for prod/dev language restrictions.
// TODO: Consider using better argument parser so that dev env vals can be defaulted.
fun main(vararg args: String) {
  check(args.size >= 6) {
    "Expected use: bazel run //scripts:download_lessons <base_url> <gcs_base_url> <gcs_bucket>" +
      " </path/to/api/secret.file> </output/dir> <cache_mode=none/lazy/force> [/cache/dir] [test,topic,ids]"
  }

  val baseUrl = args[0]
  val gcsBaseUrl = args[1]
  val gcsBucket = args[2]
  val apiSecretPath = args[3]
  val outputDirPath = args[4]
  val cacheModeLine = args[5]
  val (cacheDirPath, force) = when (val cacheMode = cacheModeLine.removePrefix("cache_mode=")) {
    "none" -> null to false
    "lazy" -> args[6] to false
    "force" -> args[6] to true
    else -> error("Invalid cache_mode: $cacheMode.")
  }
  val cacheDir = cacheDirPath?.let {
    File(cacheDirPath).absoluteFile.normalize().also {
      check(it.exists() && it.isDirectory) { "Expected cache directory to exist: $cacheDirPath." }
    }
  }
  val outputDir = File(outputDirPath).absoluteFile.normalize().also {
    check(it.exists() && it.isDirectory) { "Expected output directory to exist: $outputDirPath." }
  }

  val baseArgCount = if (cacheDirPath == null) 6 else 7
  val testTopicIds = args.getOrNull(baseArgCount)?.split(',')?.toSet() ?: setOf()
  val apiSecretFile = File(apiSecretPath).absoluteFile.normalize().also {
    check(it.exists() && it.isFile) { "Expected API secret file to exist: $apiSecretPath." }
  }
  val apiSecret = apiSecretFile.readText().trim()
  val downloader =
    DownloadLessons(baseUrl, gcsBaseUrl, gcsBucket, apiSecret, cacheDir, force, testTopicIds)
  downloader.downloadLessons(outputDir)
}

class DownloadLessons(
  gaeBaseUrl: String,
  gcsBaseUrl: String,
  gcsBucket: String,
  apiSecret: String,
  private val cacheDir: File?,
  private val forceCacheLoad: Boolean,
  testTopicIds: Set<String>
) {
  private val threadPool by lazy {
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
  }
  private val coroutineDispatcher by lazy { threadPool.asCoroutineDispatcher() }
  private val gcsService by lazy { GcsService(gcsBaseUrl, gcsBucket) }
  private val imageDownloader by lazy { ImageDownloader(gcsService, coroutineDispatcher) }
  private val androidEndpoint: GaeAndroidEndpoint by lazy {
    GaeAndroidEndpointJsonImpl(
      apiSecret,
      gaeBaseUrl,
      cacheDir,
      forceCacheLoad,
      coroutineDispatcher,
      topicDependencies = topicDependenciesTable + testTopicIds.associateWith { setOf() },
      imageDownloader
    )
  }
  private val textFormat by lazy { TextFormat.printer() }

  fun downloadLessons(outputDir: File) {
    val downloadJob = CoroutineScope(coroutineDispatcher).launch { downloadAllLessons(outputDir) }
    runBlocking {
      try {
        downloadJob.join()
      } finally {
        shutdownBlocking()
      }
    }
  }

  private suspend fun downloadAllLessons(outputDir: File) {
    when {
      cacheDir == null -> println("Config: Not using a local disk directory for asset caching.")
      !forceCacheLoad -> println("Config: Using ${cacheDir.path}/ for caching assets across runs.")
      else -> {
        println(
          "Config: Using ${cacheDir.path}/ for caching assets across runs, with no latest updating."
        )
      }
    }

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

    println()
    val listContentMessage = "Sending topic list download request"
    val extraDotsThatCanFitForList = CONSOLE_COLUMN_COUNT - listContentMessage.length
    var lastDotCount = 0
    print(listContentMessage)
    val listResponse =
      androidEndpoint.fetchTopicListAsync(listRequest) { finishCount, totalCount ->
        val dotCount = (extraDotsThatCanFitForList * finishCount) / totalCount
        val dotsToAdd = dotCount - lastDotCount
        if (dotsToAdd > 0) {
          print(".".repeat(dotsToAdd))
          lastDotCount = dotCount
        }
      }.await()
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

    println()
    val contentRequest =
      createDownloadContentRequest(downloadableTopics, defaultLanguage, supportedLanguages)
    val contentMessage = "Requesting to download ${contentRequest.identifiersCount} content items"
    val extraDotsThatCanFitForContent = CONSOLE_COLUMN_COUNT - contentMessage.length
    lastDotCount = 0
    print(contentMessage)
    val contentResponse =
      androidEndpoint.fetchTopicContentAsync(contentRequest) { finishCount, totalCount ->
        val dotCount = (extraDotsThatCanFitForContent * finishCount) / totalCount
        val dotsToAdd = dotCount - lastDotCount
        if (dotsToAdd > 0) {
          print(".".repeat(dotsToAdd))
          lastDotCount = dotCount
        }
      }.await()
    println()

    val successfulResults = contentResponse.downloadResultsList.filter {
      it.resultTypeCase != SKIPPED_FROM_FAILURE && it.resultTypeCase != SKIPPED_SHOULD_RETRY
    }
    println("${successfulResults.size}/${contentResponse.downloadResultsCount} succeeded.")

    println()
    println("Writing successful results to: ${outputDir.path}/...")
    val protoV2Dir = File(outputDir, "protov2").also { it.mkdir() }
    val textProtoV2Dir = File(protoV2Dir, "textproto").also { it.mkdir() }
    val binaryProtoV2Dir = File(protoV2Dir, "binary").also { it.mkdir() }
    // NOTE: The 'protov2' values written here are not exactly the app's protov2 definitions (since
    // those haven't been defined yet). They're just exact copies of the emulated server's
    // responses.
    val writeAsyncResults = successfulResults.map { result ->
      when (result.resultTypeCase) {
        TOPIC_SUMMARY ->
          writeProtosAsync(protoV2Dir, result.topicSummary.id, result.topicSummary)
        REVISION_CARD ->
          writeProtosAsync(protoV2Dir, result.revisionCard.id.collapse(), result.revisionCard)
        CONCEPT_CARD ->
          writeProtosAsync(protoV2Dir, result.conceptCard.skillId, result.conceptCard)
        EXPLORATION ->
          writeProtosAsync(protoV2Dir, result.exploration.id, result.exploration)
        REVISION_CARD_LANGUAGE_PACK -> {
          writeProtosAsync(
            protoV2Dir,
            result.revisionCardLanguagePack.id.collapse(),
            result.revisionCardLanguagePack
          )
        }
        CONCEPT_CARD_LANGUAGE_PACK -> {
          writeProtosAsync(
            protoV2Dir, result.conceptCardLanguagePack.id.collapse(), result.conceptCardLanguagePack
          )
        }
        EXPLORATION_LANGUAGE_PACK -> {
          writeProtosAsync(
            protoV2Dir, result.explorationLanguagePack.id.collapse(), result.explorationLanguagePack
          )
        }
        QUESTION_ID_LIST, QUESTION, QUESTION_LANGUAGE_PACK ->
          error("Questions aren't yet supported.")
        SKIPPED_SHOULD_RETRY, SKIPPED_FROM_FAILURE, RESULTTYPE_NOT_SET, null ->
          error("Encountered unexpected result: $result.")
      }
    }
    writeAsyncResults.awaitAll() // Wait for all proto writes to finish.
    println("Written proto locations:")
    println("- Proto v2 text protos can be found in: ${textProtoV2Dir.path}")
    println("- Proto v2 binary protos can be found in: ${binaryProtoV2Dir.path}")

    println()
    val imagesDir = File(outputDir, "images").also { it.mkdir() }
    val imageReferences = contentResponse.collectImageReferences().distinct()
    val baseImageMessage = "Downloading ${imageReferences.size} images"
    val extraDotsThatCanFitForImages = CONSOLE_COLUMN_COUNT - baseImageMessage.length
    lastDotCount = 0
    print(baseImageMessage)
    imageReferences.downloadAllAsync(imagesDir) { finishCount, totalCount ->
      val dotCount = (extraDotsThatCanFitForImages * finishCount) / totalCount
      val dotsToAdd = dotCount - lastDotCount
      if (dotsToAdd > 0) {
        print(".".repeat(dotsToAdd))
        lastDotCount = dotCount
      }
    }.await()
    println()
    println("Images downloaded to: ${imagesDir.path}/.")
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

  private fun writeProtosAsync(
    protoV2Dir: File, baseName: String, message: Message
  ): Deferred<Any> {
    val textProtoV2Dir = File(protoV2Dir, "textproto")
    val binaryProtoV2Dir = File(protoV2Dir, "binary")
    return CoroutineScope(coroutineDispatcher).async {
      writeTextProto(textProtoV2Dir, baseName, message)
      writeBinaryProto(binaryProtoV2Dir, baseName, message)
    }
  }

  private suspend fun writeTextProto(destDir: File, baseName: String, message: Message) {
    withContext(Dispatchers.IO) {
      File(destDir, "$baseName.textproto").also {
        check(!it.exists()) { "Destination file already exists: ${it.path}." }
      }.outputStream().bufferedWriter().use { textFormat.print(message, it) }
    }
  }

  private suspend fun writeBinaryProto(destDir: File, baseName: String, message: Message) {
    withContext(Dispatchers.IO) {
      File(destDir, "$baseName.pb").also {
        check(!it.exists()) { "Destination file already exists: ${it.path}." }
      }.outputStream().use(message::writeTo)
    }
  }

  private fun Collection<ImageReference>.downloadAllAsync(
    destDir: File, reportProgress: (Int, Int) -> Unit
  ): Deferred<Any> {
    val totalCount = size
    val channel = Channel<Int>()
    channel.consumeAsFlow().withIndex().onEach { (index, _) ->
      reportProgress(index + 1, totalCount)
    }.launchIn(CoroutineScope(coroutineDispatcher))
    return CoroutineScope(coroutineDispatcher).async {
      mapIndexed { index, reference -> reference.downloadAsync(destDir, index, channel) }.awaitAll()
      channel.close()
    }
  }

  private fun ImageReference.downloadAsync(
    destDir: File, index: Int, reportProgressChannel: SendChannel<Int>
  ): Deferred<Any> {
    return CoroutineScope(coroutineDispatcher).async {
      val imageData =
        imageDownloader.retrieveImageContentAsync(
          container.entityType, imageType, container.entityId, filename
        ).await()
      reportProgressChannel.send(index)
      withContext(Dispatchers.IO) { File(destDir, filename).writeBytes(imageData) }
    }
  }

  private fun shutdownBlocking() {
    coroutineDispatcher.close()
    threadPool.tryShutdownFully(timeout = 5, unit = TimeUnit.SECONDS)
  }

  private data class ImageContainer(val entityType: EntityType, val entityId: String)

  private data class ImageReference(
    val container: ImageContainer, val imageType: ImageType, val filename: String
  )

  private companion object {
    private val INVALID_LANGUAGE_TYPES =
      listOf(LanguageType.LANGUAGE_CODE_UNSPECIFIED, LanguageType.UNRECOGNIZED)
    private val CLIENT_CONTEXT = AndroidClientContextDto.newBuilder().apply {
      appVersionName = checkNotNull(DownloadLessons::class.qualifiedName)
      appVersionCode = 0
    }.build()
    private const val CONSOLE_COLUMN_COUNT = 80

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

    private fun SubtopicPageIdDto.collapse(): String = "${topicId}_$subtopicIndex"

    private fun createLocalizedRevisionCardId(
      id: SubtopicPageIdDto,
      language: LanguageType
    ): LocalizedRevisionCardIdDto {
      return LocalizedRevisionCardIdDto.newBuilder().apply {
        this.id = id
        this.language = language
      }.build()
    }

    private fun LocalizedRevisionCardIdDto.collapse(): String =
      "${id.collapse()}_${language.collapse()}"

    private fun createLocalizedExplorationId(
      explorationId: String,
      language: LanguageType
    ): LocalizedExplorationIdDto {
      return LocalizedExplorationIdDto.newBuilder().apply {
        this.explorationId = explorationId
        this.language = language
      }.build()
    }

    private fun LocalizedExplorationIdDto.collapse(): String =
      "${explorationId}_${language.collapse()}"

    private fun createLocalizedConceptCardId(
      skillId: String,
      language: LanguageType
    ): LocalizedConceptCardIdDto {
      return LocalizedConceptCardIdDto.newBuilder().apply {
        this.skillId = skillId
        this.language = language
      }.build()
    }

    private fun LocalizedConceptCardIdDto.collapse(): String = "${skillId}_${language.collapse()}"

    private fun LanguageType.collapse(): String {
      return when (this) {
        LanguageType.ENGLISH -> "en"
        LanguageType.ARABIC -> "ar"
        LanguageType.HINDI -> "hi"
        LanguageType.HINGLISH -> "hi-en"
        LanguageType.BRAZILIAN_PORTUGUESE -> "pt-br"
        LanguageType.SWAHILI -> "sw"
        LanguageType.NIGERIAN_PIDGIN -> "pcm"
        LanguageType.LANGUAGE_CODE_UNSPECIFIED, LanguageType.UNRECOGNIZED ->
          error("Invalid language type: $this.")
      }
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

    private fun TopicContentResponseDto.collectImageReferences(): List<ImageReference> =
      downloadResultsList.flatMap { it.collectImageReferences() }

    private fun DownloadResultDto.collectImageReferences(): List<ImageReference> {
      return when (resultTypeCase) {
        SKIPPED_SHOULD_RETRY, SKIPPED_FROM_FAILURE -> emptyList()
        TOPIC_SUMMARY -> topicSummary.collectImageReferences()
        REVISION_CARD -> revisionCard.collectImageReferences()
        CONCEPT_CARD -> conceptCard.collectImageReferences()
        EXPLORATION -> exploration.collectImageReferences()
        QUESTION_ID_LIST -> emptyList() // No translations for a question ID list.
        QUESTION -> question.collectImageReferences()
        REVISION_CARD_LANGUAGE_PACK -> revisionCardLanguagePack.collectImageReferences()
        CONCEPT_CARD_LANGUAGE_PACK -> conceptCardLanguagePack.collectImageReferences()
        EXPLORATION_LANGUAGE_PACK -> explorationLanguagePack.collectImageReferences()
        QUESTION_LANGUAGE_PACK -> questionLanguagePack.collectImageReferences()
        RESULTTYPE_NOT_SET, null -> error("Encountered invalid result: $this.")
      }
    }

    private fun DownloadableTopicSummaryDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.TOPIC, entityId = id)
      return localizations.collectImageReferences(container) +
        storySummariesList.flatMap { it.collectImageReferences() } +
        referencedSkillsList.flatMap { it.collectImageReferences() }
    }

    private fun RevisionCardDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.REVISION_CARD, entityId = id.topicId)
      return defaultLocalization.collectImageReferences(container)
    }

    private fun ConceptCardDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.CONCEPT_CARD, entityId = skillId)
      return defaultLocalization.collectImageReferences(container)
    }

    private fun ExplorationDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.EXPLORATION, entityId = id)
      return defaultLocalization.collectImageReferences(container)
    }

    private fun QuestionDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.QUESTION, entityId = id)
      return defaultLocalization.collectImageReferences(container)
    }

    private fun RevisionCardLanguagePackDto.collectImageReferences(): List<ImageReference> {
      val container =
        ImageContainer(entityType = EntityType.REVISION_CARD, entityId = id.id.topicId)
      return localization.collectImageReferences(container)
    }

    private fun ConceptCardLanguagePackDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.CONCEPT_CARD, entityId = id.skillId)
      return localization.collectImageReferences(container)
    }

    private fun ExplorationLanguagePackDto.collectImageReferences(): List<ImageReference> {
      val container =
        ImageContainer(entityType = EntityType.EXPLORATION, entityId = id.explorationId)
      return localization.collectImageReferences(container)
    }

    private fun QuestionLanguagePackDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.QUESTION, entityId = id.questionId)
      return localization.collectImageReferences(container)
    }

    private fun StorySummaryDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.STORY, entityId = id)
      return localizations.collectImageReferences(container) +
        chaptersList.flatMap { it.collectImageReferences() }
    }

    private fun ChapterSummaryDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.CHAPTER, entityId = explorationId)
      return localizations.collectImageReferences(container)
    }

    private fun SkillSummaryDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(entityType = EntityType.SKILL, entityId = id)
      return localizations.collectImageReferences(container)
    }

    private fun ContentLocalizationsDto.collectImageReferences(
      container: ImageContainer
    ): List<ImageReference> {
      return defaultMapping.collectImageReferences(container) +
        localizationsList.flatMap { it.collectImageReferences(container) }
    }

    private fun ContentLocalizationDto.collectImageReferences(
      container: ImageContainer
    ): List<ImageReference> {
      return localizedImageList.collectImageReferences(container) +
        (thumbnail.takeIf { hasThumbnail() }?.collectImageReferences(container) ?: emptyList())
    }

    private fun ReferencedImageListDto.collectImageReferences(container: ImageContainer) =
      referencedImagesList.map { it.convertToImageReference(container) }

    private fun ThumbnailDto.collectImageReferences(container: ImageContainer) =
      listOf(referencedImage.convertToImageReference(container, imageType = ImageType.THUMBNAIL))

    private fun ReferencedImageDto.convertToImageReference(
      container: ImageContainer, imageType: ImageType = ImageType.HTML_IMAGE
    ): ImageReference = ImageReference(container, imageType, filename)
  }
}

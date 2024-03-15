package org.oppia.android.scripts.assets

import com.google.protobuf.Message
import com.google.protobuf.TextFormat
import java.io.File
import java.util.concurrent.ConcurrentHashMap
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
import org.oppia.android.scripts.assets.DtoProtoToLegacyProtoConverter.convertToConceptCardList
import org.oppia.android.scripts.assets.DtoProtoToLegacyProtoConverter.convertToExploration
import org.oppia.android.scripts.assets.DtoProtoToLegacyProtoConverter.convertToStoryRecord
import org.oppia.android.scripts.assets.DtoProtoToLegacyProtoConverter.convertToSubtopicRecord
import org.oppia.android.scripts.assets.DtoProtoToLegacyProtoConverter.convertToTopicIdList
import org.oppia.android.scripts.assets.DtoProtoToLegacyProtoConverter.convertToTopicRecord
import org.oppia.android.scripts.gae.gcs.GcsService
import org.oppia.android.scripts.gae.gcs.GcsService.ImageContainerType
import org.oppia.android.scripts.gae.gcs.GcsService.ImageType
import org.oppia.android.scripts.gae.proto.ImageDownloader
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.Builder as DownloadReqStructIdDtoBuilder
import org.oppia.proto.v1.api.DownloadRequestStructureIdentifierDto.StructureTypeCase
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
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.SKIPPED_DOES_NOT_EXIST
import org.oppia.proto.v1.api.TopicContentResponseDto.DownloadResultDto.ResultTypeCase.TOPIC_SUMMARY
import org.oppia.proto.v1.structure.BaseAnswerGroupDto
import org.oppia.proto.v1.structure.BaseSolutionDto
import org.oppia.proto.v1.structure.ChapterSummaryDto
import org.oppia.proto.v1.structure.ConceptCardDto
import org.oppia.proto.v1.structure.ConceptCardDto.WorkedExampleDto
import org.oppia.proto.v1.structure.ConceptCardLanguagePackDto
import org.oppia.proto.v1.structure.ContentLocalizationDto
import org.oppia.proto.v1.structure.ContentLocalizationsDto
import org.oppia.proto.v1.structure.DragAndDropSortInputInstanceDto
import org.oppia.proto.v1.structure.ExplorationDto
import org.oppia.proto.v1.structure.ExplorationLanguagePackDto
import org.oppia.proto.v1.structure.HintDto
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase
import org.oppia.proto.v1.structure.ItemSelectionInputInstanceDto
import org.oppia.proto.v1.structure.ListOfSetsOfTranslatableHtmlContentIdsDto
import org.oppia.proto.v1.structure.LocalizableTextDto
import org.oppia.proto.v1.structure.OutcomeDto
import org.oppia.proto.v1.structure.QuestionDto
import org.oppia.proto.v1.structure.QuestionLanguagePackDto
import org.oppia.proto.v1.structure.ReferencedImageDto
import org.oppia.proto.v1.structure.ReferencedImageListDto
import org.oppia.proto.v1.structure.RevisionCardDto
import org.oppia.proto.v1.structure.RevisionCardLanguagePackDto
import org.oppia.proto.v1.structure.SetOfTranslatableHtmlContentIdsDto
import org.oppia.proto.v1.structure.SkillSummaryDto
import org.oppia.proto.v1.structure.StateDto
import org.oppia.proto.v1.structure.StorySummaryDto
import org.oppia.proto.v1.structure.TextInputInstanceDto
import org.oppia.proto.v1.structure.ThumbnailDto
import org.oppia.proto.v1.structure.TranslatableHtmlContentIdDto
import org.oppia.proto.v1.structure.TranslatableSetOfNormalizedStringDto
import org.oppia.proto.v1.structure.UpcomingTopicSummaryDto

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
  private val blockingDispatcher by lazy {
    Executors.newSingleThreadExecutor().asCoroutineDispatcher()
  }
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
  private val imageRepairer by lazy { ImageRepairer() }
  // TODO: Convert ByteArray to DownloadedImage for better analysis?
  private val memoizedLoadedImageData by lazy { ConcurrentHashMap<File, ByteArray>() }

  fun downloadLessons(outputDir: File) {
    val downloadJob = CoroutineScope(coroutineDispatcher).launch { downloadAllLessons(outputDir) }
    runBlocking {
      downloadJob.invokeOnCompletion { exception ->
        exception?.printStackTrace()
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
    val requestedLanguages = setOf(
      LanguageType.ARABIC,
      LanguageType.BRAZILIAN_PORTUGUESE,
      LanguageType.NIGERIAN_PIDGIN
    )
    val listRequest = TopicListRequestDto.newBuilder().apply {
      protoVersion = ProtoVersionProvider.createLatestTopicListProtoVersion()
      clientContext = CLIENT_CONTEXT
      compatibilityContext = ProtoVersionProvider.createCompatibilityContext()
      // No structures are considered already downloaded. TODO: Integrate with local files cache?
      requestedDefaultLanguage = defaultLanguage
//      addAllRequiredAdditionalLanguages(requestedLanguages)
      addAllSupportedAdditionalLanguages(requestedLanguages)
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
    println()

    val downloadableTopics = listResponse.availableTopicsList.filter { availableTopic ->
      availableTopic.availabilityTypeCase == DOWNLOADABLE_TOPIC
    }.map { it.downloadableTopic.topicSummary }
    val upcomingTopics = listResponse.futureTopicsList.map { it.topicSummary }
    val downloadableTopicIds = downloadableTopics.map { it.id }
    val futureTopicIds = listResponse.futureTopicsList.map { it.topicId }
    println(
      "Downloaded topic results: ${listResponse.availableTopicsCount} topics are available," +
        " ${downloadableTopics.size} are downloadable, IDs: $downloadableTopicIds." +
        " ${futureTopicIds.size} topics will later be available, IDs: $futureTopicIds."
    )

    println()
    val contentRequest =
      createDownloadContentRequest(downloadableTopics, defaultLanguage, requestedLanguages.toList())
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

    val revisionCardPackRequestResults =
      contentResponse.downloadResultsList.filter {
        it.identifier.structureTypeCase == StructureTypeCase.REVISION_CARD_LANGUAGE_PACK
      }
    val conceptCardPackRequestResults =
      contentResponse.downloadResultsList.filter {
        it.identifier.structureTypeCase == StructureTypeCase.CONCEPT_CARD_LANGUAGE_PACK
      }
    val explorationPackRequestResults =
      contentResponse.downloadResultsList.filter {
        it.identifier.structureTypeCase == StructureTypeCase.EXPLORATION_LANGUAGE_PACK
      }
    val revisionCardPackRequestResultsByLanguage =
      revisionCardPackRequestResults.groupBy { it.identifier.revisionCardLanguagePack.language }
    val conceptCardPackRequestResultsByLanguage =
      conceptCardPackRequestResults.groupBy { it.identifier.conceptCardLanguagePack.language }
    val explorationPackRequestResultsByLanguage =
      explorationPackRequestResults.groupBy { it.identifier.explorationLanguagePack.language }

    val topicRequestCount =
      contentResponse.downloadResultsList.count {
        it.identifier.structureTypeCase == StructureTypeCase.TOPIC_SUMMARY_ID
      }
    val revisionCardRequestCount =
      contentResponse.downloadResultsList.count {
        it.identifier.structureTypeCase == StructureTypeCase.REVISION_CARD
      }
    val conceptCardRequestCount =
      contentResponse.downloadResultsList.count {
        it.identifier.structureTypeCase == StructureTypeCase.CONCEPT_CARD
      }
    val explorationRequestCount =
      contentResponse.downloadResultsList.count {
        it.identifier.structureTypeCase == StructureTypeCase.EXPLORATION
      }
    val revisionCardPackRequestCount = revisionCardPackRequestResults.size
    val conceptCardPackRequestCount = conceptCardPackRequestResults.size
    val explorationPackRequestCount = explorationPackRequestResults.size

    // Print diagnostics about the download results.
    val successfulResults = contentResponse.downloadResultsList.filter {
      it.resultTypeCase != SKIPPED_FROM_FAILURE && it.resultTypeCase != SKIPPED_SHOULD_RETRY
    }
    val revisionCardPackSuccessResults =
      successfulResults.filter { it.resultTypeCase == REVISION_CARD_LANGUAGE_PACK }
    val conceptCardPackSuccessResults =
      successfulResults.filter { it.resultTypeCase == CONCEPT_CARD_LANGUAGE_PACK }
    val explorationPackSuccessResults =
      successfulResults.filter { it.resultTypeCase == EXPLORATION_LANGUAGE_PACK }

    val topicSuccessCount = successfulResults.count { it.resultTypeCase == TOPIC_SUMMARY }
    val revisionCardSuccessCount = successfulResults.count { it.resultTypeCase == REVISION_CARD }
    val conceptCardSuccessCount = successfulResults.count { it.resultTypeCase == CONCEPT_CARD }
    val explorationSuccessCount = successfulResults.count { it.resultTypeCase == EXPLORATION }
    val revisionCardPackSuccessCount = revisionCardPackSuccessResults.size
    val conceptCardPackSuccessCount = conceptCardPackSuccessResults.size
    val explorationPackSuccessCount = explorationPackSuccessResults.size

    println("Download results:")
    println("- $topicSuccessCount/$topicRequestCount topics succeeded")
    println("- $revisionCardSuccessCount/$revisionCardRequestCount revision cards succeeded")
    println("- $conceptCardSuccessCount/$conceptCardRequestCount concept cards succeeded")
    println("- $explorationSuccessCount/$explorationRequestCount explorations succeeded")
    println(
      "- $revisionCardPackSuccessCount/$revisionCardPackRequestCount revision card language" +
        " packs succeeded"
    )
    requestedLanguages.forEach { languageType ->
      val results = revisionCardPackRequestResultsByLanguage.getValue(languageType)
      val successCount = results.count {
        it.resultTypeCase != SKIPPED_FROM_FAILURE && it.resultTypeCase != SKIPPED_SHOULD_RETRY
      }
      println("  - ${languageType.name}: $successCount/${results.size} succeeded")
    }
    println(
      "- $conceptCardPackSuccessCount/$conceptCardPackRequestCount concept card language packs" +
        " succeeded"
    )
    requestedLanguages.forEach { languageType ->
      val results = conceptCardPackRequestResultsByLanguage.getValue(languageType)
      val successCount = results.count {
        it.resultTypeCase != SKIPPED_FROM_FAILURE && it.resultTypeCase != SKIPPED_SHOULD_RETRY
      }
      println("  - ${languageType.name}: $successCount/${results.size} succeeded")
    }
    println(
      "- $explorationPackSuccessCount/$explorationPackRequestCount exploration language packs" +
        " succeeded"
    )
    requestedLanguages.forEach { languageType ->
      val results = explorationPackRequestResultsByLanguage.getValue(languageType)
      val successCount = results.count {
        it.resultTypeCase != SKIPPED_FROM_FAILURE && it.resultTypeCase != SKIPPED_SHOULD_RETRY
      }
      println("  - ${languageType.name}: $successCount/${results.size} succeeded")
    }

    println()
    println("Writing successful results to: ${outputDir.path}/...")
    val protoV1Dir = File(outputDir, "protov1").also { it.mkdir() }
    val protoV2Dir = File(outputDir, "protov2").also { it.mkdir() }
    val textProtoV2Dir = File(protoV2Dir, "textproto").also { it.mkdir() }
    val binaryProtoV2Dir = File(protoV2Dir, "binary").also { it.mkdir() }
    val textProtoV1Dir = File(protoV1Dir, "textproto").also { it.mkdir() }
    val binaryProtoV1Dir = File(protoV1Dir, "binary").also { it.mkdir() }
    // NOTE: The 'protov2' values written here are not exactly the app's protov2 definitions (since
    // those haven't been defined yet). They're just exact copies of the emulated server's
    // responses.
    val writeProtoV2AsyncResults = successfulResults.mapNotNull { result ->
      when (result.resultTypeCase) {
        TOPIC_SUMMARY -> writeProtosAsync(protoV2Dir, result.topicSummary.id, result.topicSummary)
        REVISION_CARD ->
          writeProtosAsync(protoV2Dir, result.revisionCard.id.collapse(), result.revisionCard)
        CONCEPT_CARD -> writeProtosAsync(protoV2Dir, result.conceptCard.skillId, result.conceptCard)
        EXPLORATION -> writeProtosAsync(protoV2Dir, result.exploration.id, result.exploration)
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
        // The result was a success, but the corresponding ID doesn't correspond to a structure.
        SKIPPED_DOES_NOT_EXIST -> null
        QUESTION_ID_LIST, QUESTION, QUESTION_LANGUAGE_PACK ->
          error("Questions aren't yet supported.")
        SKIPPED_SHOULD_RETRY, SKIPPED_FROM_FAILURE, RESULTTYPE_NOT_SET, null ->
          error("Encountered unexpected result: $result.")
      }
    }

    val topicSummaries = successfulResults.filter {
      it.resultTypeCase == TOPIC_SUMMARY
    }.associate { it.topicSummary.id to it.topicSummary }
    val storySummaries = topicSummaries.values.flatMap { it.storySummariesList }

    val revisionCards = successfulResults.filter {
      it.resultTypeCase == REVISION_CARD
    }.map { it.revisionCard }
    val revisionCardPacks = successfulResults.filter {
      it.resultTypeCase == REVISION_CARD_LANGUAGE_PACK
    }.groupBy(
      keySelector = { it.revisionCardLanguagePack.id.id },
      valueTransform = { it.revisionCardLanguagePack }
    )

    val conceptCards = successfulResults.filter {
      it.resultTypeCase == CONCEPT_CARD
    }.map { it.conceptCard }
    val conceptCardPacks = successfulResults.filter {
      it.resultTypeCase == CONCEPT_CARD_LANGUAGE_PACK
    }.groupBy(
      keySelector = { it.conceptCardLanguagePack.id.skillId },
      valueTransform = { it.conceptCardLanguagePack }
    )

    val explorations = successfulResults.filter {
      it.resultTypeCase == EXPLORATION
    }.map { it.exploration }
    val explorationPacks = successfulResults.filter {
      it.resultTypeCase == EXPLORATION_LANGUAGE_PACK
    }.groupBy(
      keySelector = { it.explorationLanguagePack.id.explorationId },
      valueTransform = { it.explorationLanguagePack }
    )

    println()
    val imagesDir = File(outputDir, "images").also { it.mkdir() }
    val imageReferences = contentResponse.collectImageReferences().distinct()
    val baseImageMessage = "Downloading ${imageReferences.size} images"
    val extraDotsThatCanFitForImages = CONSOLE_COLUMN_COUNT - baseImageMessage.length
    lastDotCount = 0
    print(baseImageMessage)
    val images = imageReferences.downloadAllAsync(imagesDir) { finishCount, totalCount ->
      val dotCount = (extraDotsThatCanFitForImages * finishCount) / totalCount
      val dotsToAdd = dotCount - lastDotCount
      if (dotsToAdd > 0) {
        print(".".repeat(dotsToAdd))
        lastDotCount = dotCount
      }
    }.await()
    println()
    println("Images downloaded to: ${imagesDir.path}/.")
    println()

    val imageSuccessCount = images.values.count { it is DownloadedImage.Succeeded }
    val imageDuplicationCount = images.values.count { it is DownloadedImage.Duplicated }
    val renamedImages = images.values.filterIsInstance<DownloadedImage.Renamed>()
    val convertedSvgImages = images.values.filterIsInstance<DownloadedImage.ConvertedSvgToPng>()
    val convertedGifImages = images.values.filterIsInstance<DownloadedImage.ConvertedGifToPng>()
    println("$imageSuccessCount/${images.size} images successfully downloaded.")
    println("$imageDuplicationCount/${images.size} images were de-duplicated.")
    println("${renamedImages.size}/${images.size} images required renaming due to conflicts.")
    println("${convertedSvgImages.size}/${images.size} images required repairing from SVG to PNG.")
    println("${convertedGifImages.size}/${images.size} images required repairing from GIF to PNG.")
    println()

    if (renamedImages.isNotEmpty()) {
      println("Please manually verify the following renamed images:")
      val destDir by lazy { File(outputDir, "image_renames").also { it.mkdir() } }
      renamedImages.forEach { renamedImage ->
        val oldFilename = renamedImage.oldFilename
        val newFilename = renamedImage.newFilename
        val resolutionDir = File(destDir, oldFilename.substringBeforeLast('.'))
        val beforeDir = File(resolutionDir, "before").also { it.mkdirs() }
        val afterDir = File(resolutionDir, "after").also { it.mkdirs() }
        val newFile = File(imagesDir, newFilename)
        val beforeFile = File(beforeDir, oldFilename)
        val afterFile = File(afterDir, newFilename)
        val oldFileData = renamedImage.readOriginalFileData()
        beforeFile.writeBytes(oldFileData)
        newFile.copyTo(afterFile, overwrite = true)
        val imageUrl =
          imageDownloader.computeImageUrl(
            renamedImage.imageRef.container.imageContainerType,
            renamedImage.imageRef.imageType,
            renamedImage.imageRef.container.entityId,
            renamedImage.imageRef.filename
          )
        println("- Image $oldFilename required repairing via renaming:")
        println("  - Before: ${beforeFile.path} (${oldFileData.size} bytes)")
        println("  - After: ${afterFile.path} (${newFile.length()} bytes)")
        println("  - Image URL: $imageUrl")
        println("  - Language: ${renamedImage.imageRef.container.language}")
      }
      println()
    }

    if (convertedSvgImages.isNotEmpty()) {
      println("Please manually verify the following converted SVG->PNG images:")
      val destDir by lazy { File(outputDir, "image_conversions").also { it.mkdir() } }
      convertedSvgImages.forEach { convertedImage ->
        val oldFilename = convertedImage.imageRef.filename
        val newFilename = convertedImage.newFilename
        val resolutionDir = File(destDir, oldFilename.substringBeforeLast('.'))
        val beforeDir = File(resolutionDir, "before").also { it.mkdirs() }
        val afterDir = File(resolutionDir, "after").also { it.mkdirs() }
        val beforeImageData = convertedImage.downloadedImageData.toByteArray()
        val afterImageData = convertedImage.convertedImageData.toByteArray()
        val beforeFile = File(beforeDir, oldFilename).also { it.writeBytes(beforeImageData) }
        val afterFile = File(afterDir, newFilename).also { it.writeBytes(afterImageData) }
        val imageUrl =
          imageDownloader.computeImageUrl(
            convertedImage.imageRef.container.imageContainerType,
            convertedImage.imageRef.imageType,
            convertedImage.imageRef.container.entityId,
            convertedImage.imageRef.filename
          )
        println("- Image $oldFilename required repairing via conversion:")
        println("  - Before: ${beforeFile.path} (${beforeImageData.size} bytes)")
        println("  - After: ${afterFile.path} (${afterImageData.size} bytes)")
        println("  - Image URL: $imageUrl")
        println("  - Language: ${convertedImage.imageRef.container.language}")
        println("  - Rendered resolution: ${convertedImage.width}x${convertedImage.height}")
      }
      println()
    }

    if (convertedGifImages.isNotEmpty()) {
      println("Please manually verify the following converted GIF->PNG images:")
      val destDir by lazy { File(outputDir, "image_conversions").also { it.mkdir() } }
      convertedGifImages.forEach { convertedImage ->
        val oldFilename = convertedImage.imageRef.filename
        val newFilename = convertedImage.newFilename
        val resolutionDir = File(destDir, oldFilename.substringBeforeLast('.'))
        val beforeDir = File(resolutionDir, "before").also { it.mkdirs() }
        val afterDir = File(resolutionDir, "after").also { it.mkdirs() }
        val beforeImageData = convertedImage.downloadedImageData.toByteArray()
        val afterImageData = convertedImage.convertedImageData.toByteArray()
        val beforeFile = File(beforeDir, oldFilename).also { it.writeBytes(beforeImageData) }
        val afterFile = File(afterDir, newFilename).also { it.writeBytes(afterImageData) }
        val imageUrl =
          imageDownloader.computeImageUrl(
            convertedImage.imageRef.container.imageContainerType,
            convertedImage.imageRef.imageType,
            convertedImage.imageRef.container.entityId,
            convertedImage.imageRef.filename
          )
        println("- Image $oldFilename required repairing via conversion:")
        println("  - Before: ${beforeFile.path} (${beforeImageData.size} bytes)")
        println("  - After: ${afterFile.path} (${afterImageData.size} bytes)")
        println("  - Image URL: $imageUrl")
        println("  - Language: ${convertedImage.imageRef.container.language}")
      }
      println()
    }

    val conceptCardImageReplacements = conceptCards.associate { dto ->
      dto.skillId to images.computeReplacements(ImageContainerType.SKILL, dto.skillId)
    }
    val writeProtoV1AsyncResults = topicSummaries.map { (topicId, topicSummary) ->
      val imageReplacements = images.computeReplacements(ImageContainerType.TOPIC, topicId)
      writeProtosAsync(protoV1Dir, topicId, topicSummary.convertToTopicRecord(imageReplacements))
    } + upcomingTopics.map { upcomingTopic ->
      val imageReplacements = images.computeReplacements(ImageContainerType.TOPIC, upcomingTopic.id)
      writeProtosAsync(
        protoV1Dir, upcomingTopic.id, upcomingTopic.convertToTopicRecord(imageReplacements)
      )
    } + storySummaries.map { storySummary ->
      val imageReplacements = images.computeReplacements(ImageContainerType.STORY, storySummary.id)
      writeProtosAsync(
        protoV1Dir, storySummary.id, storySummary.convertToStoryRecord(imageReplacements)
      )
    } + revisionCards.map { revisionCard ->
      val imageReplacements =
        images.computeReplacements(ImageContainerType.TOPIC, revisionCard.id.topicId)
      val topicSummary = topicSummaries.getValue(revisionCard.id.topicId)
      val subtopicSummary =
        topicSummary.subtopicSummariesList.find { it.index == revisionCard.id.subtopicIndex }
          ?: error("Could not find subtopic summary for revision card: ${revisionCard.id}.")
      // TODO: The listOf() default here allows cards to have no translations.
      val packs = revisionCardPacks[revisionCard.id] ?: listOf()
      writeProtosAsync(
        protoV1Dir,
        revisionCard.id.collapse(),
        revisionCard.convertToSubtopicRecord(imageReplacements, subtopicSummary, packs)
      )
    } + writeProtosAsync(
      protoV1Dir,
      baseName = "skills",
      convertToConceptCardList(
        conceptCardImageReplacements,
        // TODO: The listOf() default here allows cards to have no translations.
        conceptCards.map { it to (conceptCardPacks[it.skillId] ?: listOf()) }
      )
    ) + explorations.map { exp ->
      val imageReplacements = images.computeReplacements(ImageContainerType.EXPLORATION, exp.id)
      val packs = explorationPacks.getValue(exp.id)
      writeProtosAsync(protoV1Dir, exp.id, exp.convertToExploration(imageReplacements, packs))
    } + writeProtosAsync(
      protoV1Dir, baseName = "topics", topicSummaries.values.convertToTopicIdList()
    )

    // Wait for all proto writes to finish.
    (writeProtoV2AsyncResults + writeProtoV1AsyncResults).awaitAll()
    println("Written proto locations:")
    println("- Proto v2 text protos can be found in: ${textProtoV2Dir.path}")
    println("- Proto v2 binary protos can be found in: ${binaryProtoV2Dir.path}")
    println("- Proto v1 text protos can be found in: ${textProtoV1Dir.path}")
    println("- Proto v1 binary protos can be found in: ${binaryProtoV1Dir.path}")

    val analyzer = CompatibilityAnalyzer(requestedLanguages + setOf(defaultLanguage))
    topicSummaries.values.forEach(analyzer::track)
    upcomingTopics.forEach(analyzer::track)
//    revisionCards.forEach(analyzer::track)
    explorations.forEach(analyzer::track)
//    conceptCards.forEach(analyzer::track)
//    revisionCardPacks.values.flatten().forEach(analyzer::track)
//    conceptCardPacks.values.flatten().forEach(analyzer::track)
    explorationPacks.values.flatten().forEach(analyzer::track)

    val issues = analyzer.scanForIssues().sorted()
    val imageInvalidExtIssues = issues.filterIsInstance<CompatibilityAnalyzer.Issue.ImageHasInvalidExtension>()
    val imageInconsistencyIssues = issues.filterIsInstance<CompatibilityAnalyzer.Issue.ImageInconsistencies>()
    val htmlInvalidTagIssues = issues.filterIsInstance<CompatibilityAnalyzer.Issue.HtmlHasInvalidTag>()
    val translationIssues = issues.filterIsInstance<CompatibilityAnalyzer.Issue.TextMissingTranslations>()
    println()
    println("${issues.size} issues were found during import. High-level break-down:")
    println("- ${imageInvalidExtIssues.size}/${issues.size} correspond to invalid image extensions")
    println("- ${imageInconsistencyIssues.size}/${issues.size} correspond to images missing across translations")
    println("- ${htmlInvalidTagIssues.size}/${issues.size} correspond to invalid tags found in HTML")
    println("- ${translationIssues.size}/${issues.size} correspond to missing translations")
    println()
    println("Images with invalid extensions:")
    imageInvalidExtIssues.groupBy { it.container }.forEach { (container, issues) ->
      println("- Within ${container.referenceString}:")
      issues.forEach { issue ->
        println("  - Image ${issue.filename} (language: ${issue.language.name}) has invalid extension: ${issue.invalidExtension}")
      }
    }
    println()
    println("Images missing across translations: (Hidden)")
//    imageInconsistencyIssues.groupBy { it.container }.forEach { (container, issues) ->
//      println("- Within ${container.referenceString}:")
//      issues.forEach { issue ->
//        val missingLangs = issue.missingLanguages.joinToString { it.name }
//        val presentLangs = issue.presentLanguages.joinToString { it.name }
//        println("  - Image ${issue.filename} exists in languages: $presentLangs, but is missing in: $missingLangs")
//      }
//    }
    println()
    println("HTML strings with invalid tags:")
    htmlInvalidTagIssues.groupBy { it.text.container }.forEach { (container, issues) ->
      println("- Within ${container.referenceString}:")
      issues.groupBy { it.language }.forEach { (language, perLangIssues) ->
        println("  - For language ${language.name}:")
        perLangIssues.forEach { issue ->
          println("    - Text with content ID ${issue.text.contentId} has references tag: ${issue.invalidTag}")
        }
      }
    }
    println()
    println("Strings missing translations:")
    translationIssues.groupBy { it.text.container }.forEach { (container, issues) ->
      println("- Within ${container.referenceString}:")
      issues.forEach { issue ->
        val missingLangs = issue.missingLanguages.joinToString { it.name }
        val presentLangs = issue.presentLanguages.joinToString { it.name }
        println("  - Text with content ID ${issue.text.contentId} exists in languages: $presentLangs, but is missing in: $missingLangs")
      }
    }

    val imageDownloadFailures = images.values.filterIsInstance<DownloadedImage.FailedCouldNotFind>()
    if (imageDownloadFailures.isNotEmpty()) {
      println()
      println("Images that failed to download:")
      imageDownloadFailures.forEach { downloadedImage ->
        val reference = downloadedImage.imageRef
        val imageUrl =
          imageDownloader.computeImageUrl(
            reference.container.imageContainerType,
            reference.imageType,
            reference.container.entityId,
            reference.filename
          )
        val language = reference.container.language
        println("- Image failed to download (could not find image, language: $language): $imageUrl")
      }
    }

    if (renamedImages.isNotEmpty() || convertedSvgImages.isNotEmpty() || convertedGifImages.isNotEmpty()) {
      println("WARNING: Images needed to be auto-fixed. Please verify that they are correct")
      println("(look at above output for specific images that require verification).")
    }

//    val translationMetrics = analyzer.computeTranslationsUsageReport()
//    val voiceoverMetrics = analyzer.computeVoiceoversUsageReport()
//    println("#".repeat(CONSOLE_COLUMN_COUNT))
//    println()
//    println("Translation statistics:")
//    translationMetrics.forEach { usageReport ->
//      println("- For ${usageReport.container.referenceString}:")
//      usageReport.languageUsage.forEach { (language, usage) ->
//        println("  - Language ${language.name} has ${usage.usageString} strings translated (${usage.percentageString})")
//      }
//    }
//    println()
//    println("Voiceover statistics:")
//    voiceoverMetrics.forEach { usageReport ->
//      println("- For ${usageReport.container.referenceString}:")
//      usageReport.languageUsage.forEach { (language, usage) ->
//        println("  - Language ${language.name} has ${usage.usageString} content strings subtitled by audio voiceovers (${usage.percentageString})")
//      }
//    }
//    println()
//    println("#".repeat(CONSOLE_COLUMN_COUNT))
//    println()
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
    ) + generateIdentifiersToDownloadConceptCards(
      topicSummary, defaultLanguage, requestedLanguages
    ) + topicSummary.toStructureIdentifier(topicSummary.contentVersion) { setTopicSummaryId(it.id) }
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
      File(destDir, "$baseName.textproto")
        .outputStream()
        .bufferedWriter()
        .use { textFormat.escapingNonAscii(false).print(message, it) }
    }
  }

  private suspend fun writeBinaryProto(destDir: File, baseName: String, message: Message) {
    withContext(Dispatchers.IO) {
      File(destDir, "$baseName.pb").outputStream().use(message::writeTo)
    }
  }

  private fun Collection<ImageReference>.downloadAllAsync(
    destDir: File, reportProgress: (Int, Int) -> Unit
  ): Deferred<Map<ImageReference, DownloadedImage>> {
    check(destDir.deleteRecursively() && destDir.mkdir()) {
      "Failed to clear & recreate image destination dir: ${destDir.path}."
    }
    val totalCount = size
    val channel = Channel<Int>()
    channel.consumeAsFlow().withIndex().onEach { (index, _) ->
      reportProgress(index + 1, totalCount)
    }.launchIn(CoroutineScope(coroutineDispatcher))
    return CoroutineScope(coroutineDispatcher).async {
      mapIndexed { index, reference ->
        reference.downloadAsync(destDir, index, channel)
      }.awaitAll().groupBy { it.imageRef }.mapValues { (_, matches) ->
        matches.single()
      }.also { channel.close() }
    }
  }

  private fun ImageReference.downloadAsync(
    destDir: File, index: Int, reportProgressChannel: SendChannel<Int>
  ): Deferred<DownloadedImage> {
    val reference = this
    return CoroutineScope(coroutineDispatcher).async {
      imageDownloader.retrieveImageContentAsync(
        container.imageContainerType, imageType, container.entityId, filename
      ).await()?.let { imageData ->
        withContext(Dispatchers.IO) {
          when (val conv = imageRepairer.convertToPng(filename, imageData)) {
            ImageRepairer.RepairedImage.NoRepairNeeded -> {
              val imageFile = File(destDir, filename)
              when {
                !imageFile.exists() -> {
                  memoizedLoadedImageData[imageFile] = imageData
                  imageFile.writeBytes(imageData)
                  DownloadedImage.Succeeded(reference)
                }
                imageFile.isImageFileSameAs(imageData) -> DownloadedImage.Duplicated(reference)
                else -> {
                  val newFile = computeNewUniqueFile(destDir, imageFile)
                  memoizedLoadedImageData[newFile] = imageData
                  newFile.writeBytes(imageData)
                  DownloadedImage.Renamed.ExistingFile(
                    reference, oldFile = imageFile, newFilename = newFile.name
                  )
                }
              }
            }
            is ImageRepairer.RepairedImage.ConvertedFromGif -> {
              val nameWithoutExt = filename.substringBeforeLast('.')
              val expectedNewImageFile = File(destDir, "$nameWithoutExt.png")
              val newImageFile = if (expectedNewImageFile.exists()) {
                if (expectedNewImageFile.isImageFileSameAs(conv.pngContents.toByteArray())) {
                  // This is a rename since the original file is being converted from SVG.
                  return@withContext DownloadedImage.Renamed.ConvertedFile(
                    reference,
                    oldFilename = filename,
                    oldFileData = imageData.toList(),
                    newFilename = expectedNewImageFile.name
                  )
                } else computeNewUniqueFile(destDir, expectedNewImageFile)
              } else expectedNewImageFile
              val newImageData = conv.pngContents.toByteArray()
              memoizedLoadedImageData[newImageFile] = newImageData
              newImageFile.writeBytes(newImageData)
              DownloadedImage.ConvertedGifToPng(
                reference,
                newFilename = newImageFile.name,
                imageData.toList(),
                conv.pngContents
              )
            }
            is ImageRepairer.RepairedImage.RenderedSvg -> {
              val nameWithoutExt = filename.substringBeforeLast('.')
              val expectedNewImageFile = File(destDir, "$nameWithoutExt.png")
              val newImageFile = if (expectedNewImageFile.exists()) {
                if (expectedNewImageFile.isImageFileSameAs(conv.pngContents.toByteArray())) {
                  // This is a rename since the original file is being converted from SVG.
                  return@withContext DownloadedImage.Renamed.ConvertedFile(
                    reference,
                    oldFilename = filename,
                    oldFileData = imageData.toList(),
                    newFilename = expectedNewImageFile.name
                  )
                } else computeNewUniqueFile(destDir, expectedNewImageFile)
              } else expectedNewImageFile
              val newImageData = conv.pngContents.toByteArray()
              memoizedLoadedImageData[newImageFile] = newImageData
              newImageFile.writeBytes(newImageData)
              DownloadedImage.ConvertedSvgToPng(
                reference,
                newFilename = newImageFile.name,
                imageData.toList(),
                conv.pngContents,
                conv.width,
                conv.height
              )
            }
          }
        }
      }.also { reportProgressChannel.send(index) } ?: DownloadedImage.FailedCouldNotFind(reference)
    }
  }

  private fun File.isImageFileSameAs(imageData: ByteArray): Boolean {
    val imageDataToCheck = memoizedLoadedImageData.getValue(this)

    // First, perform a byte-equality check.
    if (imageDataToCheck.toList() == imageData.toList()) return true

    // Second, perform an image-equality check (exact match).
    return imageRepairer.areEqualImages(extension, imageDataToCheck, imageData)
  }

  private fun computeNewUniqueFile(destDir: File, imageFile: File): File {
    val imageBaseFilename = imageFile.nameWithoutExtension
    val copyCount = destDir.listFiles()?.count {
      it.nameWithoutExtension.startsWith(imageBaseFilename)
    }?.plus(1) ?: 0 // Zero shouldn't actually happen in practice.
    val newFilename = "${imageBaseFilename}_$copyCount.${imageFile.extension}"
    return File(destDir, newFilename)
  }

  private sealed class DownloadedImage {
    abstract val imageRef: ImageReference

    data class Succeeded(override val imageRef: ImageReference): DownloadedImage()

    data class Duplicated(override val imageRef: ImageReference): DownloadedImage()

    sealed class Renamed: DownloadedImage() {
      abstract val oldFilename: String
      abstract val newFilename: String

      abstract fun readOriginalFileData(): ByteArray

      data class ExistingFile(
        override val imageRef: ImageReference, val oldFile: File, override val newFilename: String
      ): Renamed() {
        override val oldFilename: String get() = oldFile.name
        override fun readOriginalFileData(): ByteArray = oldFile.readBytes()
      }

      data class ConvertedFile(
        override val imageRef: ImageReference,
        override val oldFilename: String,
        val oldFileData: List<Byte>,
        override val newFilename: String
      ): Renamed() {
        override fun readOriginalFileData(): ByteArray = oldFileData.toByteArray()
      }
    }

    data class ConvertedSvgToPng(
      override val imageRef: ImageReference,
      val newFilename: String,
      val downloadedImageData: List<Byte>,
      val convertedImageData: List<Byte>,
      val width: Int,
      val height: Int
    ): DownloadedImage()

    data class ConvertedGifToPng(
      override val imageRef: ImageReference,
      val newFilename: String,
      val downloadedImageData: List<Byte>,
      val convertedImageData: List<Byte>
    ): DownloadedImage()

    data class FailedCouldNotFind(override val imageRef: ImageReference): DownloadedImage()
  }

  private fun shutdownBlocking() {
    coroutineDispatcher.close()
    threadPool.tryShutdownFully(timeout = 5, unit = TimeUnit.SECONDS)
  }

  private data class ImageContainer(
    val imageContainerType: ImageContainerType, val entityId: String, val language: LanguageType
  )

  private data class ImageReference(
    val container: ImageContainer, val imageType: ImageType, val filename: String
  )

  private fun Map<ImageReference, DownloadedImage>.computeReplacements(
    imageContainerType: ImageContainerType, entityId: String
  ): Map<String, String> {
    return filterKeys { ref ->
      ref.container.imageContainerType == imageContainerType && ref.container.entityId == entityId
    }.mapValues { (_, image) ->
      when (image) {
        is DownloadedImage.ConvertedSvgToPng -> image.imageRef.filename to image.newFilename
        is DownloadedImage.ConvertedGifToPng -> image.imageRef.filename to image.newFilename
        is DownloadedImage.Renamed -> image.oldFilename to image.newFilename
        is DownloadedImage.Duplicated, is DownloadedImage.FailedCouldNotFind,
        is DownloadedImage.Succeeded -> null
      }
    }.values.filterNotNull().groupBy { (oldFilename, _) ->
      oldFilename
    }.mapValues { (oldFilename, matches) ->
      val uniqueReplacements = matches.mapTo(mutableSetOf()) { (_, replacement) -> replacement }
      uniqueReplacements.singleOrNull()
        ?: error("Multiple files correspond to image: $oldFilename: $uniqueReplacements.")
    }.also { imageReplacements ->
      val cyclicKeys = imageReplacements.keys.filter { it in imageReplacements.values }
      check(cyclicKeys.isEmpty()) {
        "Cycle(s) found in image replacements map: $cyclicKeys."
      }
    }
  }

  private class CompatibilityAnalyzer(private val expectedLanguages: Set<LanguageType>) {
    private val texts by lazy { mutableListOf<TextReference>() }
    private val localizations by lazy { mutableListOf<Localizations>() }

    fun track(dto: DownloadableTopicSummaryDto) {
      val container = Container.Topic(dto.id)
      if (dto.hasName()) texts += TextReference.Name(container, dto.name.contentId)
      if (dto.hasDescription()) texts += TextReference.Description(container, dto.description.contentId)
      if (dto.hasLocalizations()) track(container, dto.localizations)
      dto.storySummariesList.forEach { track(container, it) }
      dto.referencedSkillsList.forEach { track(container, it) }
    }

    fun track(dto: UpcomingTopicSummaryDto) {
      val container = Container.Topic(dto.id)
      if (dto.hasName()) texts += TextReference.Name(container, dto.name.contentId)
      if (dto.hasDescription()) texts += TextReference.Description(container, dto.description.contentId)
      if (dto.hasLocalizations()) track(container, dto.localizations)
    }

    fun track(dto: RevisionCardDto) {
      val topic = Container.Topic(dto.id.topicId)
      val container = Container.RevisionCard(topic, dto.id.subtopicIndex)
      if (dto.hasTitle()) texts += TextReference.Title(container, dto.title.contentId)
      if (dto.hasContent()) texts += TextReference.Content(container, dto.content.contentId)
      if (dto.hasDefaultLocalization()) track(container, dto.defaultLocalization)
    }

    fun track(dto: ConceptCardDto) {
      val container = Container.ConceptCard(dto.skillId)
      if (dto.hasDescription()) texts += TextReference.Description(container, dto.description.contentId)
      if (dto.hasExplanation()) texts += TextReference.Explanation(container, dto.explanation.contentId)
      if (dto.hasDefaultLocalization()) track(container, dto.defaultLocalization)
      dto.workedExamplesList.forEachIndexed { index, example -> track(container, index, example) }
    }

    fun track(dto: ExplorationDto) {
      val container = Container.Exploration(dto.id)
      if (dto.hasTitle()) texts += TextReference.Title(container, dto.title.contentId)
      if (dto.hasDefaultLocalization()) track(container, dto.defaultLocalization)
      dto.statesMap.forEach { (name, state) -> track(container, name, state) }
    }

    fun track(dto: RevisionCardLanguagePackDto) {
      val topic = Container.Topic(dto.id.id.topicId)
      val container = Container.RevisionCard(topic, dto.id.id.subtopicIndex)
      if (dto.hasLocalization()) track(container, dto.localization)
    }

    fun track(dto: ConceptCardLanguagePackDto) {
      val container = Container.ConceptCard(dto.id.skillId)
      if (dto.hasLocalization()) track(container, dto.localization)
    }

    fun track(dto: ExplorationLanguagePackDto) {
      val container = Container.Exploration(dto.id.explorationId)
      if (dto.hasLocalization()) track(container, dto.localization)
    }

    fun scanForIssues(): List<Issue> {
      return scanForInvalidExtensions() +
        scanForImageInconsistencies() +
        scanForHtmlInvalidTags() +
        scanForTextMissingTranslations()
    }

    private fun scanForInvalidExtensions(): List<Issue.ImageHasInvalidExtension> {
      return localizations.filterIsInstance<Localizations.ImageReferences>().flatMap { images ->
        images.filenames.filter { it.endsWith(".gif", ignoreCase = true) }.map { filename ->
          Issue.ImageHasInvalidExtension(images.container, images.language, filename, invalidExtension = "gif")
        }
      } + localizations.filterIsInstance<Localizations.Thumbnail>().mapNotNull { thumbnail ->
        thumbnail.thumbnailFilename.takeIf { it.endsWith(".gif", ignoreCase = true) }?.let { filename ->
          Issue.ImageHasInvalidExtension(thumbnail.container, thumbnail.language, filename, invalidExtension = "gif")
        }
      }
    }

    private fun scanForImageInconsistencies(): List<Issue.ImageInconsistencies> {
      return localizations.filterIsInstance<Localizations.ImageReferences>().flatMap { images ->
        images.filenames.map { filename ->
          (images.container to filename) to images.language
        }
      }.groupBy(
        keySelector = { (key, _) -> key },
        valueTransform = { (_, value) -> value }
      ).map { (key, languages) ->
        val (container, filename) = key
        val presentLanguages = languages.toSet()
        Issue.ImageInconsistencies(container, filename, presentLanguages, expectedLanguages - presentLanguages)
      }
    }

    private fun scanForHtmlInvalidTags(): List<Issue.HtmlHasInvalidTag> {
      val invalidTags = listOf(
        "oppia-noninteractive-link",
        "oppia-noninteractive-tabs",
        "oppia-noninteractive-video",
        "oppia-noninteractive-collapsible"
      )
      val textsByContentId = texts.groupBy { it.container.findRoot() to it.contentId }
      return localizations.filterIsInstance<Localizations.Translations>().flatMap { translations ->
        translations.translations.flatMap { translation ->
          translation.htmls.flatMap { html ->
            invalidTags.map { invalidTag ->
              (html to invalidTag).takeIf { invalidTag in html }
            }
          }.filterNotNull().map { (html, invalidTag) ->
            // Exactly one text reference should correspond to this content ID among all in the root
            // container.
            Issue.HtmlHasInvalidTag(
              translations.language,
              textsByContentId.getValue(translations.container.findRoot() to translation.contentId).single(),
              html,
              invalidTag
            )
          }
        }
      }
    }

    private fun scanForTextMissingTranslations(): List<Issue.TextMissingTranslations> {
      val textLanguages = localizations.filterIsInstance<Localizations.Translations>().flatMap { translations ->
        translations.translations.map { (translations.container.findRoot() to it.contentId) to translations }
      }.groupBy(
        keySelector = { (key, _) -> key },
        valueTransform = { (_, value) -> value.language }
      )
      return texts.mapNotNull { text ->
        val languages = textLanguages[text.container.findRoot() to text.contentId]?.toSet() ?: emptySet()
        val missingLanguages = expectedLanguages - languages
        if (missingLanguages.isNotEmpty()) {
          Issue.TextMissingTranslations(text, languages, missingLanguages)
        } else null
      }
    }

    fun computeTranslationsUsageReport(): List<MetricsReport.TranslationUsage> {
      val textLanguages = localizations.filterIsInstance<Localizations.Translations>().flatMap { translations ->
        translations.translations.map { (translations.container.findRoot() to it.contentId) to translations }
      }.groupBy(
        keySelector = { (key, _) -> key },
        valueTransform = { (_, value) -> value.language }
      )
      val textsByContainer = texts.groupBy { it.container }
      return texts.flatMap { text ->
        val translations = textLanguages[text.container.findRoot() to text.contentId]?.map {
          it to text
        } ?: emptyList()
        translations
      }.groupBy(
        keySelector = { (language, text) -> text.container to language },
        valueTransform = { (_, text) -> text.contentId }
      ).mapValues { (key, contentIds) ->
        val (container, _) = key
        MetricsReport.Usage(
          usedCount = contentIds.size, totalCount = textsByContainer.getValue(container).size
        )
      }.entries.groupBy(
        keySelector = { (key, _) ->
          val (container, language) = key
          container.findRoot() to language
        },
        valueTransform = { (_, metrics) -> metrics }
      ).mapValues { (_, metrics) ->
        metrics.reduce(MetricsReport.Usage::combineWith)
      }.entries.groupBy(
        keySelector = { (key, _) ->
          val (container, _) = key
          container
        },
        valueTransform = { (key, combinedMetrics) ->
          val (_, language) = key
          language to combinedMetrics
        }
      ).entries.map { (container, values) ->
        MetricsReport.TranslationUsage(container, values.toMap())
      }
    }

    fun computeVoiceoversUsageReport(): List<MetricsReport.VoiceoverUsage> {
      val voiceoverLanguages = localizations.filterIsInstance<Localizations.Voiceovers>().flatMap { voiceovers ->
        voiceovers.contentIds.map { (voiceovers.container.findRoot() to it) to voiceovers }
      }.groupBy(
        keySelector = { (key, _) -> key },
        valueTransform = { (_, value) -> value.language }
      )
      val textsByContainer = texts.groupBy { it.container }
      return texts.flatMap { text ->
        val voiceovers = voiceoverLanguages[text.container.findRoot() to text.contentId]?.map {
          it to text
        } ?: emptyList()
        voiceovers
      }.groupBy(
        keySelector = { (language, text) -> text.container to language },
        valueTransform = { (_, text) -> text.contentId }
      ).mapValues { (key, contentIds) ->
        val (container, _) = key
        MetricsReport.Usage(
          usedCount = contentIds.size, totalCount = textsByContainer.getValue(container).size
        )
      }.entries.groupBy(
        keySelector = { (key, _) ->
          val (container, language) = key
          container.findRoot() to language
        },
        valueTransform = { (_, metrics) -> metrics }
      ).mapValues { (_, metrics) ->
        metrics.reduce(MetricsReport.Usage::combineWith)
      }.entries.groupBy(
        keySelector = { (key, _) ->
          val (container, _) = key
          container
        },
        valueTransform = { (key, combinedMetrics) ->
          val (_, language) = key
          language to combinedMetrics
        }
      ).entries.map { (container, values) ->
        MetricsReport.VoiceoverUsage(container, values.toMap())
      }
    }

    private fun track(topic: Container.Topic, dto: StorySummaryDto) {
      val container = Container.Story(topic, dto.id)
      if (dto.hasTitle()) texts += TextReference.Title(container, dto.title.contentId)
      if (dto.hasDescription()) texts += TextReference.Description(container, dto.description.contentId)
      if (dto.hasLocalizations()) track(container, dto.localizations)
      dto.chaptersList.forEach { track(container, it) }
    }

    private fun track(story: Container.Story, dto: ChapterSummaryDto) {
      val container = Container.Chapter(story, dto.explorationId)
      if (dto.hasTitle()) texts += TextReference.Title(container, dto.title.contentId)
      if (dto.hasDescription()) texts += TextReference.Description(container, dto.description.contentId)
      if (dto.hasLocalizations()) track(container, dto.localizations)
    }

    private fun track(topic: Container.Topic, dto: SkillSummaryDto) {
      val container = Container.Skill(topic, dto.id)
      if (dto.hasName()) texts += TextReference.Name(container, dto.name.contentId)
      if (dto.hasLocalizations()) track(container, dto.localizations)
    }

    private fun track(conceptCard: Container.ConceptCard, index: Int, dto: WorkedExampleDto) {
      val container = Container.WorkedExample(conceptCard, index)
      if (dto.hasQuestion()) texts += TextReference.Question(container, dto.question.contentId)
      if (dto.hasExplanation()) texts += TextReference.Explanation(container, dto.explanation.contentId)
    }

    private fun track(exploration: Container.Exploration, name: String, dto: StateDto) {
      val container = Container.State(exploration, name)
      if (dto.hasContent()) texts += TextReference.Content(container, dto.content.contentId)
      when (dto.interaction.interactionTypeCase) {
        InteractionTypeCase.CONTINUE_INSTANCE -> {
          val interaction = dto.interaction.continueInstance
          val args = interaction.customizationArgs
          if (args.hasButtonText()) texts += TextReference.CustomizationArg.ButtonText(container, args.buttonText.contentId)
//          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
        }
        InteractionTypeCase.FRACTION_INPUT -> {
          val interaction = dto.interaction.fractionInput
          val args = interaction.customizationArgs
          val solution = interaction.solution
          if (args.hasPlaceholder()) texts += TextReference.CustomizationArg.Placeholder(container, args.placeholder.contentId)
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          if (interaction.hasSolution() && solution.hasBaseSolution()) track(container, solution.baseSolution)
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { index, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, index, answerGroup.baseAnswerGroup)
          }
        }
        InteractionTypeCase.ITEM_SELECTION_INPUT -> {
          val interaction = dto.interaction.itemSelectionInput
          val args = interaction.customizationArgs
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          args.choicesList.forEachIndexed { index, choice ->
            texts += TextReference.CustomizationArg.Choice(container, choice.contentId, index)
          }
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { agIndex, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, agIndex, answerGroup.baseAnswerGroup)
            answerGroup.ruleSpecsList.forEachIndexed { rsIndex, ruleSpecDto ->
              when (ruleSpecDto.ruleTypeCase) {
                ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.EQUALS -> {
                  val ruleSpec = ruleSpecDto.equals
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.CONTAINS_AT_LEAST_ONE_OF -> {
                  val ruleSpec = ruleSpecDto.containsAtLeastOneOf
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.DOES_NOT_CONTAIN_AT_LEAST_ONE_OF -> {
                  val ruleSpec = ruleSpecDto.doesNotContainAtLeastOneOf
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_PROPER_SUBSET_OF -> {
                  val ruleSpec = ruleSpecDto.isProperSubsetOf
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                ItemSelectionInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
                  error("Invalid rule spec: $ruleSpecDto.")
              }
            }
          }
        }
        InteractionTypeCase.MULTIPLE_CHOICE_INPUT -> {
          val interaction = dto.interaction.multipleChoiceInput
          val args = interaction.customizationArgs
//          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          args.choicesList.forEachIndexed { index, choice ->
            texts += TextReference.CustomizationArg.Choice(container, choice.contentId, index)
          }
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { index, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, index, answerGroup.baseAnswerGroup)
          }
        }
        InteractionTypeCase.NUMERIC_INPUT -> {
          val interaction = dto.interaction.numericInput
          val solution = interaction.solution
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          if (interaction.hasSolution() && solution.hasBaseSolution()) track(container, solution.baseSolution)
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { index, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, index, answerGroup.baseAnswerGroup)
          }
        }
        InteractionTypeCase.TEXT_INPUT -> {
          val interaction = dto.interaction.textInput
          val args = interaction.customizationArgs
          val solution = interaction.solution
          if (args.hasPlaceholder()) texts += TextReference.CustomizationArg.Placeholder(container, args.placeholder.contentId)
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          if (interaction.hasSolution() && solution.hasBaseSolution()) track(container, solution.baseSolution)
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { agIndex, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, agIndex, answerGroup.baseAnswerGroup)
            answerGroup.ruleSpecsList.forEachIndexed { rsIndex, ruleSpecDto ->
              when (ruleSpecDto.ruleTypeCase) {
                TextInputInstanceDto.RuleSpecDto.RuleTypeCase.EQUALS -> {
                  val ruleSpec = ruleSpecDto.equals
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                TextInputInstanceDto.RuleSpecDto.RuleTypeCase.STARTS_WITH -> {
                  val ruleSpec = ruleSpecDto.startsWith
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                TextInputInstanceDto.RuleSpecDto.RuleTypeCase.CONTAINS -> {
                  val ruleSpec = ruleSpecDto.contains
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                TextInputInstanceDto.RuleSpecDto.RuleTypeCase.FUZZY_EQUALS -> {
                  val ruleSpec = ruleSpecDto.fuzzyEquals
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                TextInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
                  error("Invalid rule spec: $ruleSpecDto.")
              }
            }
          }
        }
        InteractionTypeCase.DRAG_AND_DROP_SORT_INPUT -> {
          val interaction = dto.interaction.dragAndDropSortInput
          val args = interaction.customizationArgs
          val solution = interaction.solution
          args.choicesList.forEachIndexed { index, choice ->
            texts += TextReference.CustomizationArg.Choice(container, choice.contentId, index)
          }
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          if (interaction.hasSolution() && solution.hasBaseSolution()) track(container, solution.baseSolution)
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { agIndex, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, agIndex, answerGroup.baseAnswerGroup)
            answerGroup.ruleSpecsList.forEachIndexed { rsIndex, ruleSpecDto ->
              when (ruleSpecDto.ruleTypeCase) {
                DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUAL_TO_ORDERING -> {
                  val ruleSpec = ruleSpecDto.isEqualToOrdering
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.IS_EQUAL_TO_ORDERING_WITH_ONE_ITEM_AT_INCORRECT_POSITION -> {
                  val ruleSpec = ruleSpecDto.isEqualToOrderingWithOneItemAtIncorrectPosition
                  if (ruleSpec.hasInput()) track(container, agIndex, rsIndex, ruleSpec.input)
                }
                DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_ELEMENT_X_AT_POSITION_Y -> {
                  val ruleSpec = ruleSpecDto.hasElementXAtPositionY
                  if (ruleSpec.hasElement()) track(container, agIndex, rsIndex, ruleSpec.element, context = "input")
                }
                DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.HAS_ELEMENT_X_BEFORE_ELEMENT_Y -> {
                  val ruleSpec = ruleSpecDto.hasElementXBeforeElementY
                  if (ruleSpec.hasConsideredElement()) track(container, agIndex, rsIndex, ruleSpec.consideredElement, context = "consideredElement")
                  if (ruleSpec.hasLaterElement()) track(container, agIndex, rsIndex, ruleSpec.laterElement, context = "laterElement")
                }
                DragAndDropSortInputInstanceDto.RuleSpecDto.RuleTypeCase.RULETYPE_NOT_SET, null ->
                  error("Invalid rule spec: $ruleSpecDto.")
              }
            }
          }
        }
        InteractionTypeCase.IMAGE_CLICK_INPUT -> {
          val interaction = dto.interaction.imageClickInput
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { index, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, index, answerGroup.baseAnswerGroup)
          }
        }
        InteractionTypeCase.RATIO_EXPRESSION_INPUT -> {
          val interaction = dto.interaction.ratioExpressionInput
          val args = interaction.customizationArgs
          val solution = interaction.solution
          if (args.hasPlaceholder()) texts += TextReference.CustomizationArg.Placeholder(container, args.placeholder.contentId)
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          if (interaction.hasSolution() && solution.hasBaseSolution()) track(container, solution.baseSolution)
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { index, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, index, answerGroup.baseAnswerGroup)
          }
        }
        InteractionTypeCase.ALGEBRAIC_EXPRESSION_INPUT -> {
          val interaction = dto.interaction.algebraicExpressionInput
          val solution = interaction.solution
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          if (interaction.hasSolution() && solution.hasBaseSolution()) track(container, solution.baseSolution)
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { index, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, index, answerGroup.baseAnswerGroup)
          }
        }
        InteractionTypeCase.MATH_EQUATION_INPUT -> {
          val interaction = dto.interaction.mathEquationInput
          val solution = interaction.solution
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          if (interaction.hasSolution() && solution.hasBaseSolution()) track(container, solution.baseSolution)
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { index, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, index, answerGroup.baseAnswerGroup)
          }
        }
        InteractionTypeCase.NUMERIC_EXPRESSION_INPUT -> {
          val interaction = dto.interaction.numericExpressionInput
          val args = interaction.customizationArgs
          val solution = interaction.solution
          if (args.hasPlaceholder()) texts += TextReference.CustomizationArg.Placeholder(container, args.placeholder.contentId)
          if (interaction.hasDefaultOutcome()) track(container, interaction.defaultOutcome)
          if (interaction.hasSolution() && solution.hasBaseSolution()) track(container, solution.baseSolution)
          interaction.hintsList.forEachIndexed { index, hint -> track(container, index, hint) }
          interaction.answerGroupsList.forEachIndexed { index, answerGroup ->
            if (answerGroup.hasBaseAnswerGroup()) track(container, index, answerGroup.baseAnswerGroup)
          }
        }
        InteractionTypeCase.END_EXPLORATION -> {} // Nothing to track.
        InteractionTypeCase.INTERACTIONTYPE_NOT_SET, null ->
          error("Invalid interaction: ${dto.interaction}.")
      }
    }

    private fun track(container: Container, dto: OutcomeDto) {
      if (dto.hasFeedback()) texts += TextReference.Feedback(container, dto.feedback.contentId)
    }

    private fun track(state: Container.State, index: Int, dto: BaseAnswerGroupDto) {
      val container = Container.AnswerGroup(state, index)
      if (dto.hasOutcome()) track(container, dto.outcome)
    }

    private fun track(state: Container.State, answerGroupIndex: Int, ruleSpecIndex: Int, dto: ListOfSetsOfTranslatableHtmlContentIdsDto) {
      val answerGroup = Container.AnswerGroup(state, answerGroupIndex)
      val ruleSpec = Container.RuleSpec(answerGroup, ruleSpecIndex)
      dto.contentIdSetsList.forEachIndexed { index, ids ->
        track(ruleSpec, ids) { "ListOfSetsOfTranslatableHtmlContentIdsDto(index=$index, $it)" }
      }
    }

    private fun track(state: Container.State, answerGroupIndex: Int, ruleSpecIndex: Int, dto: SetOfTranslatableHtmlContentIdsDto) {
      val answerGroup = Container.AnswerGroup(state, answerGroupIndex)
      val ruleSpec = Container.RuleSpec(answerGroup, ruleSpecIndex)
      track(ruleSpec, dto)
    }

    private fun track(container: Container, dto: SetOfTranslatableHtmlContentIdsDto, createExtraContext: (String) -> String = { it }) {
      dto.contentIdsList.forEachIndexed { index, id ->
        track(container, id, context = createExtraContext("SetOfTranslatableHtmlContentIds(idx=$index)"))
      }
    }

    private fun track(state: Container.State, answerGroupIndex: Int, ruleSpecIndex: Int, dto: TranslatableSetOfNormalizedStringDto) {
      val answerGroup = Container.AnswerGroup(state, answerGroupIndex)
      val ruleSpec = Container.RuleSpec(answerGroup, ruleSpecIndex)
      texts += TextReference.RuleInputTranslatableHtmlContentId(ruleSpec, dto.contentId, context = "TranslatableSetOfNormalizedString")
    }

    private fun track(state: Container.State, index: Int, dto: HintDto) {
      val container = Container.Hint(state, index)
      if (dto.hasHintContent()) texts += TextReference.Content(container, dto.hintContent.contentId)
    }

    private fun track(container: Container, dto: BaseSolutionDto) {
      if (dto.hasExplanation()) texts += TextReference.SolutionExplanation(container, dto.explanation.contentId)
    }

    private fun track(container: Container, dto: TranslatableHtmlContentIdDto, context: String) {
      texts += TextReference.RuleInputTranslatableHtmlContentId(container, dto.contentId, context)
    }

    private fun track(state: Container.State, answerGroupIndex: Int, ruleSpecIndex: Int, dto: TranslatableHtmlContentIdDto, context: String) {
      val answerGroup = Container.AnswerGroup(state, answerGroupIndex)
      val ruleSpec = Container.RuleSpec(answerGroup, ruleSpecIndex)
      texts += TextReference.RuleInputTranslatableHtmlContentId(ruleSpec, dto.contentId, context)
    }

    private fun track(container: Container, dto: ContentLocalizationsDto) {
      if (dto.hasDefaultMapping()) track(container, dto.defaultMapping)
      dto.localizationsList.forEach { track(container, it) }
    }

    private fun track(container: Container, dto: ContentLocalizationDto) {
      if (dto.hasThumbnail()) localizations += Localizations.Thumbnail(container, dto.language, dto.thumbnail.referencedImage.filename)
      localizations += Localizations.Translations(
        container,
        dto.language,
        dto.localizableTextContentMappingMap.map { (contentId, localizableText) ->
          when (localizableText.dataFormatCase) {
            LocalizableTextDto.DataFormatCase.SINGLE_LOCALIZABLE_TEXT ->
              Localizations.Translation.Single(contentId, localizableText.singleLocalizableText.text)
            LocalizableTextDto.DataFormatCase.SET_OF_LOCALIZABLE_TEXT ->
              Localizations.Translation.Multi(contentId, localizableText.setOfLocalizableText.textList)
            LocalizableTextDto.DataFormatCase.DATAFORMAT_NOT_SET, null ->
              error("Invalid localizable text: $localizableText.")
          }
        }
      )
      localizations += Localizations.Voiceovers(container, dto.language, dto.voiceoverContentMappingMap.keys)
      if (dto.hasLocalizedImageList()) {
        localizations += Localizations.ImageReferences(container, dto.language, dto.localizedImageList.referencedImagesList.map { it.filename }.toSet())
      }
    }

    sealed class Container: Comparable<Container> {
      abstract val parent: Container?
      protected abstract val impliedTypeOrder: Int
      protected abstract val selfReferenceString: String

      val referenceString: String
        get() = parent?.let { "$selfReferenceString in ${it.referenceString}" } ?: selfReferenceString

      override fun compareTo(other: Container): Int = COMPARATOR.compare(this, other)

      protected abstract fun compareToInternal(other: Container): Int

      data class Topic(val topicId: String): Container() {
        override val parent = null
        override val selfReferenceString = "topic $topicId"
        override val impliedTypeOrder = 0

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as Topic)

        private companion object {
          private val COMPARATOR = compareBy(Topic::topicId)
        }
      }

      data class Story(override val parent: Topic, val storyId: String): Container() {
        override val selfReferenceString = "story $storyId"
        override val impliedTypeOrder = 1

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as Story)

        private companion object {
          private val COMPARATOR = compareBy(Story::storyId)
        }
      }

      data class Chapter(override val parent: Story, val explorationId: String): Container() {
        override val selfReferenceString = "chapter (exp: $explorationId)"
        override val impliedTypeOrder = 2

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as Chapter)

        private companion object {
          private val COMPARATOR = compareBy(Chapter::explorationId)
        }
      }

      data class Skill(override val parent: Topic, val skillId: String): Container() {
        override val selfReferenceString = "skill $skillId"
        override val impliedTypeOrder = 3

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as Skill)

        private companion object {
          private val COMPARATOR = compareBy(Skill::skillId)
        }
      }

      data class RevisionCard(override val parent: Topic, val index: Int): Container() {
        override val selfReferenceString = "revision card (subtopic: $index)"
        override val impliedTypeOrder = 4

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as RevisionCard)

        private companion object {
          private val COMPARATOR = compareBy(RevisionCard::index)
        }
      }

      data class ConceptCard(val skillId: String): Container() {
        override val parent = null
        override val selfReferenceString = "concept card (skill: $skillId)"
        override val impliedTypeOrder = 5

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as ConceptCard)

        private companion object {
          private val COMPARATOR = compareBy(ConceptCard::skillId)
        }
      }

      data class WorkedExample(override val parent: ConceptCard, val index: Int): Container() {
        override val selfReferenceString = "worked example $index"
        override val impliedTypeOrder = 6

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as WorkedExample)

        private companion object {
          private val COMPARATOR = compareBy(WorkedExample::index)
        }
      }

      data class Exploration(val explorationId: String): Container() {
        override val parent = null
        override val selfReferenceString = "exploration $explorationId"
        override val impliedTypeOrder = 7

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as Exploration)

        private companion object {
          private val COMPARATOR = compareBy(Exploration::explorationId)
        }
      }

      data class State(override val parent: Exploration, val name: String): Container() {
        override val selfReferenceString = "state '$name'"
        override val impliedTypeOrder = 8

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as State)

        private companion object {
          private val COMPARATOR = compareBy(State::name)
        }
      }

      data class AnswerGroup(override val parent: State, val index: Int): Container() {
        override val selfReferenceString = "answer group $index"
        override val impliedTypeOrder = 9

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as AnswerGroup)

        private companion object {
          private val COMPARATOR = compareBy(AnswerGroup::index)
        }
      }

      data class RuleSpec(override val parent: AnswerGroup, val index: Int): Container() {
        override val selfReferenceString = "rule spec $index"
        override val impliedTypeOrder = 10

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as RuleSpec)

        private companion object {
          private val COMPARATOR = compareBy(RuleSpec::index)
        }
      }

      data class Hint(override val parent: State, val index: Int): Container() {
        override val selfReferenceString = "hint $index"
        override val impliedTypeOrder = 11

        override fun compareToInternal(other: Container): Int =
          COMPARATOR.compare(this, other as Hint)

        private companion object {
          private val COMPARATOR = compareBy(Hint::index)
        }
      }

      companion object {
        private val COMPARATOR = compareBy(Container::impliedTypeOrder).thenBy(Container::parent).thenComparing(Container::compareToInternal)
      }
    }

    sealed class TextReference: Comparable<TextReference> {
      abstract val container: Container
      abstract val contentId: String
      protected abstract val impliedTypeOrder: Int

      val referenceString: String get() = "$typeName in ${container.referenceString}"

      protected abstract val typeName: String

      override fun compareTo(other: TextReference): Int = COMPARATOR.compare(this, other)

      data class Name(override val container: Container, override val contentId: String): TextReference() {
        override val typeName = "name"
        override val impliedTypeOrder = 0
      }

      data class Title(override val container: Container, override val contentId: String): TextReference() {
        override val typeName = "title"
        override val impliedTypeOrder = 1
      }

      data class Description(override val container: Container, override val contentId: String): TextReference() {
        override val typeName = "description"
        override val impliedTypeOrder = 2
      }

      data class Content(override val container: Container, override val contentId: String): TextReference() {
        override val typeName = "content"
        override val impliedTypeOrder = 3
      }

      data class Explanation(override val container: Container, override val contentId: String): TextReference() {
        override val typeName = "explanation"
        override val impliedTypeOrder = 4
      }

      data class Question(override val container: Container, override val contentId: String): TextReference() {
        override val typeName = "question"
        override val impliedTypeOrder = 5
      }

      data class Feedback(override val container: Container, override val contentId: String): TextReference() {
        override val typeName = "feedback"
        override val impliedTypeOrder = 6
      }

      data class SolutionExplanation(override val container: Container, override val contentId: String): TextReference() {
        override val typeName = "solution explanation"
        override val impliedTypeOrder = 7
      }

      data class RuleInputTranslatableHtmlContentId(override val container: Container, override val contentId: String, val context: String): TextReference() {
        override val typeName = "rule input ($context)"
        override val impliedTypeOrder = 8
      }

      sealed class CustomizationArg: TextReference() {
        override val typeName get() = "customization arg ($argName)"

        protected abstract val argName: String

        data class ButtonText(override val container: Container, override val contentId: String) : CustomizationArg() {
          override val argName = "button text"
          override val impliedTypeOrder = 9
        }

        data class Placeholder(override val container: Container, override val contentId: String) : CustomizationArg() {
          override val argName = "placeholder"
          override val impliedTypeOrder = 10
        }

        data class Choice(override val container: Container, override val contentId: String, val index: Int) : CustomizationArg() {
          override val argName = "choice $index"
          override val impliedTypeOrder = 11
        }
      }

      private companion object {
        private val COMPARATOR = compareBy(TextReference::container).thenBy(TextReference::contentId).thenBy(TextReference::impliedTypeOrder)
      }
    }

    sealed class Localizations {
      abstract val container: Container
      abstract val language: LanguageType

      data class Translations(
        override val container: Container,
        override val language: LanguageType,
        val translations: List<Translation>
      ): Localizations()
      data class Voiceovers(override val container: Container, override val language: LanguageType, val contentIds: Set<String>): Localizations()
      data class ImageReferences(override val container: Container, override val language: LanguageType, val filenames: Set<String>): Localizations()
      data class Thumbnail(override val container: Container, override val language: LanguageType, val thumbnailFilename: String): Localizations()

      sealed class Translation {
        abstract val contentId: String
        abstract val htmls: List<String>

        data class Single(override val contentId: String, val html: String): Translation() {
          override val htmls: List<String> get() = listOf(html)
        }
        data class Multi(override val contentId: String, override val htmls: List<String>): Translation()
      }
    }

    sealed class Issue: Comparable<Issue> {
      protected abstract val referenceContainer: Container
      protected abstract val impliedTypeOrder: Int

      override fun compareTo(other: Issue): Int = COMPARATOR.compare(this, other)

      protected abstract fun compareToInternal(other: Issue): Int

      data class ImageHasInvalidExtension(val container: Container, val language: LanguageType, val filename: String, val invalidExtension: String): Issue() {
        override val referenceContainer = container
        override val impliedTypeOrder: Int = 0

        override fun compareToInternal(other: Issue): Int =
          COMPARATOR.compare(this, other as ImageHasInvalidExtension)

        private companion object {
          private val COMPARATOR = compareBy(ImageHasInvalidExtension::language).thenBy(ImageHasInvalidExtension::filename).thenBy(ImageHasInvalidExtension::invalidExtension)
        }
      }

      data class ImageInconsistencies(val container: Container, val filename: String, val presentLanguages: Set<LanguageType>, val missingLanguages: Set<LanguageType>): Issue() {
        override val referenceContainer = container
        override val impliedTypeOrder: Int = 1

        override fun compareToInternal(other: Issue): Int =
          COMPARATOR.compare(this, other as ImageInconsistencies)

        private companion object {
          private val COMPARATOR = compareBy(ImageInconsistencies::filename)
        }
      }

      data class HtmlHasInvalidTag(val language: LanguageType, val text: TextReference, val html: String, val invalidTag: String): Issue() {
        override val referenceContainer = text.container
        override val impliedTypeOrder: Int = 2

        override fun compareToInternal(other: Issue): Int =
          COMPARATOR.compare(this, other as HtmlHasInvalidTag)

        private companion object {
          private val COMPARATOR = compareBy(HtmlHasInvalidTag::language).thenBy(HtmlHasInvalidTag::invalidTag).thenBy(HtmlHasInvalidTag::text)
        }
      }

      data class TextMissingTranslations(val text: TextReference, val presentLanguages: Set<LanguageType>, val missingLanguages: Set<LanguageType>): Issue() {
        override val referenceContainer = text.container
        override val impliedTypeOrder: Int = 3

        override fun compareToInternal(other: Issue): Int =
          COMPARATOR.compare(this, other as TextMissingTranslations)

        private companion object {
          private val COMPARATOR = compareBy(TextMissingTranslations::text)
        }
      }

      private companion object {
        private val COMPARATOR =
          compareBy(Issue::referenceContainer)
            .thenBy(Issue::impliedTypeOrder)
            .thenComparing(Issue::compareToInternal)
      }
    }

    sealed class MetricsReport {
      data class TranslationUsage(val container: Container, val languageUsage: Map<LanguageType, Usage>): MetricsReport()
      data class VoiceoverUsage(val container: Container, val languageUsage: Map<LanguageType, Usage>): MetricsReport()

      data class Usage(val usedCount: Int, val totalCount: Int) {
        val ratio: Float get() = usedCount.toFloat() / totalCount.toFloat()
        val roundedPercentage: Float get() = (ratio * 1000).toInt() / 10.0f
        val percentageString: String get() = if (totalCount != 0) "$roundedPercentage%" else "N/A"
        val usageString: String get() = "$usedCount/$totalCount"

        fun combineWith(other: Usage): Usage =
          Usage(usedCount = usedCount + other.usedCount, totalCount = totalCount + other.totalCount)
      }
    }
  }

  private companion object {
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
        SKIPPED_SHOULD_RETRY, SKIPPED_FROM_FAILURE, SKIPPED_DOES_NOT_EXIST -> emptyList()
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
      val container = ImageContainer(imageContainerType = ImageContainerType.TOPIC, entityId = id, language = localizations.defaultMapping.language)
      return localizations.collectImageReferences(container) +
        storySummariesList.flatMap { it.collectImageReferences(localizations.defaultMapping.language) } +
        referencedSkillsList.flatMap { it.collectImageReferences() }
    }

    private fun RevisionCardDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(imageContainerType = ImageContainerType.TOPIC, entityId = id.topicId, language = defaultLocalization.language)
      return defaultLocalization.collectImageReferences(container)
    }

    private fun ConceptCardDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(imageContainerType = ImageContainerType.SKILL, entityId = skillId, language = defaultLocalization.language)
      return defaultLocalization.collectImageReferences(container)
    }

    private fun ExplorationDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(imageContainerType = ImageContainerType.EXPLORATION, entityId = id, language = defaultLocalization.language)
      return defaultLocalization.collectImageReferences(container)
    }

    private fun QuestionDto.collectImageReferences(): List<ImageReference> {
      // TODO: Should be using skill ID here?
      val container = ImageContainer(imageContainerType = ImageContainerType.SKILL, entityId = id, language = defaultLocalization.language)
      return defaultLocalization.collectImageReferences(container)
    }

    private fun RevisionCardLanguagePackDto.collectImageReferences(): List<ImageReference> {
      val container =
        ImageContainer(imageContainerType = ImageContainerType.TOPIC, entityId = id.id.topicId, language = id.language)
      return localization.collectImageReferences(container)
    }

    private fun ConceptCardLanguagePackDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(imageContainerType = ImageContainerType.SKILL, entityId = id.skillId, language = id.language)
      return localization.collectImageReferences(container)
    }

    private fun ExplorationLanguagePackDto.collectImageReferences(): List<ImageReference> {
      val container =
        ImageContainer(imageContainerType = ImageContainerType.EXPLORATION, entityId = id.explorationId, language = id.language)
      return localization.collectImageReferences(container)
    }

    private fun QuestionLanguagePackDto.collectImageReferences(): List<ImageReference> {
      // TODO: Should be using skill ID here?
      val container = ImageContainer(imageContainerType = ImageContainerType.SKILL, entityId = id.questionId, language = id.language)
      return localization.collectImageReferences(container)
    }

    private fun StorySummaryDto.collectImageReferences(defaultLanguage: LanguageType): List<ImageReference> {
      val container = ImageContainer(imageContainerType = ImageContainerType.STORY, entityId = id, language = defaultLanguage)
      return localizations.collectImageReferences(container) +
        chaptersList.flatMap { it.collectImageReferences(this@collectImageReferences.id, defaultLanguage) }
    }

    private fun ChapterSummaryDto.collectImageReferences(storyId: String, defaultLanguage: LanguageType): List<ImageReference> {
      val container = ImageContainer(imageContainerType = ImageContainerType.STORY, entityId = storyId, language = defaultLanguage)
      return localizations.collectImageReferences(container)
    }

    private fun SkillSummaryDto.collectImageReferences(): List<ImageReference> {
      val container = ImageContainer(imageContainerType = ImageContainerType.SKILL, entityId = id, language = localizations.defaultMapping.language)
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

    private fun CompatibilityAnalyzer.Container.findRoot(): CompatibilityAnalyzer.Container = generateSequence(this) { it.parent }.last()
  }
}

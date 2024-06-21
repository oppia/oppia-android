package org.oppia.android.scripts.assets

import com.google.protobuf.TextFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.gae.GaeAndroidEndpoint
import org.oppia.android.scripts.gae.GaeAndroidEndpointJsonImpl
import org.oppia.android.scripts.gae.gcs.GcsService
import org.oppia.android.scripts.gae.proto.ImageDownloader
import org.oppia.android.scripts.gae.proto.ProtoVersionProvider
import org.oppia.android.scripts.proto.DownloadListVersions
import org.oppia.android.scripts.proto.DownloadListVersions.ChapterInfo
import org.oppia.android.scripts.proto.DownloadListVersions.SkillInfo
import org.oppia.android.scripts.proto.DownloadListVersions.StoryInfo
import org.oppia.android.scripts.proto.DownloadListVersions.SubtopicInfo
import org.oppia.android.scripts.proto.DownloadListVersions.TopicInfo
import org.oppia.proto.v1.api.AndroidClientContextDto
import org.oppia.proto.v1.api.TopicListRequestDto
import org.oppia.proto.v1.api.TopicListResponseDto
import org.oppia.proto.v1.api.TopicListResponseDto.AvailableTopicDto.AvailabilityTypeCase.DOWNLOADABLE_TOPIC
import org.oppia.proto.v1.structure.ChapterSummaryDto
import org.oppia.proto.v1.structure.DownloadableTopicSummaryDto
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.proto.v1.structure.StorySummaryDto
import org.oppia.proto.v1.structure.SubtopicSummaryDto
import java.io.File

// TODO: hook up to language configs for prod/dev language restrictions.
// TODO: Consider using better argument parser so that dev env vals can be defaulted.
// TODO: verify that images aren't changed after upload, but this needs to be confirmed (that is, if they need to be changed a new image is added to GCS, instead).
fun main(vararg args: String) {
  check(args.size == 6) {
    "Expected use: bazel run //scripts:download_lesson_list <base_url> <gcs_base_url>" +
      " <gcs_bucket> </path/to/api/secret.file> </path/to/output_list.textproto>" +
      " </path/to/api/debug/dir>"
  }

  val baseUrl = args[0]
  val gcsBaseUrl = args[1]
  val gcsBucket = args[2]
  val apiSecretPath = args[3]
  val outputFilePath = args[4]
  val apiDebugPath = args[5]
  val apiSecretFile = File(apiSecretPath).absoluteFile.normalize().also {
    check(it.exists() && it.isFile) { "Expected API secret file to exist: $apiSecretPath." }
  }
  val outputFile = File(outputFilePath).absoluteFile.normalize()
  val apiDebugDir = File(apiDebugPath).absoluteFile.normalize().also {
    check(if (!it.exists()) it.mkdirs() else it.isDirectory) {
      "Expected API debug directory to exist or to be creatable: $apiDebugPath."
    }
  }

  val apiSecret = apiSecretFile.readText().trim()

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val downloader = LessonListDownloader(
      baseUrl, gcsBaseUrl, gcsBucket, apiSecret, apiDebugDir, scriptBgDispatcher
    )
    runBlocking { downloader.downloadLessonListAsync(outputFile).await() }
  }
}

class LessonListDownloader(
  gaeBaseUrl: String,
  gcsBaseUrl: String,
  gcsBucket: String,
  apiSecret: String,
  private val apiDebugDir: File,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
) {
  private val gcsService by lazy { GcsService(gcsBaseUrl, gcsBucket) }
  private val imageDownloader by lazy { ImageDownloader(gcsService, scriptBgDispatcher) }
  private val androidEndpoint: GaeAndroidEndpoint by lazy {
    GaeAndroidEndpointJsonImpl(
      apiSecret,
      gaeBaseUrl,
      apiDebugDir,
      forceCacheLoad = false,
      scriptBgDispatcher,
      imageDownloader,
      forcedVersions = null // Always load latest when creating the pin versions list.
    )
  }

  fun downloadLessonListAsync(lessonListOutputFile: File): Deferred<Unit> {
    return CoroutineScope(scriptBgDispatcher).async {
      println("Config: Using ${apiDebugDir.path}/ for storing API responses (for debugging).")

      val listResponse = downloadTopicListResponseDto()
      println()

      println("Writing captured lesson structure versions to:")
      println(lessonListOutputFile.path)
      withContext(Dispatchers.IO) {
        lessonListOutputFile.outputStream().bufferedWriter().use {
          TextFormat.printer().print(listResponse.captureVersions(), it)
        }
      }
    }
  }

  private suspend fun downloadTopicListResponseDto(): TopicListResponseDto {
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

    return listResponse
  }

  private companion object {
    private val CLIENT_CONTEXT = AndroidClientContextDto.newBuilder().apply {
      appVersionName = checkNotNull(LessonListDownloader::class.qualifiedName)
      appVersionCode = 0
    }.build()
    private const val CONSOLE_COLUMN_COUNT = 80

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

    // TODO: Migrate deps over to the data coming from GAE (since it *is* present).
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

    private fun TopicListResponseDto.captureVersions(): DownloadListVersions {
      val downloadableTopics = availableTopicsList.filter { availableTopic ->
        availableTopic.availabilityTypeCase == DOWNLOADABLE_TOPIC
      }.map { it.downloadableTopic.topicSummary }
      val topicInfos = downloadableTopics.map { it.captureVersions() }

      // Ensure that duplicate skill structures are actually the same for a given ID.
      val allReferencedSkills =
        downloadableTopics.flatMap { it.referencedSkillsList }.groupBy { it.id }
      val uniqueReferencedSkills = allReferencedSkills.mapValues { (skillId, dupedSkills) ->
        val distinctSkills = dupedSkills.distinct()
        check(distinctSkills.size == 1) {
          "Expected all references to skill $skillId to be the same skill structure."
        }
        return@mapValues distinctSkills.single()
      }

      val skillInfos = uniqueReferencedSkills.map { (skillId, skillSummary) ->
        SkillInfo.newBuilder().apply {
          this.id = skillId
          this.contentVersion = skillSummary.contentVersion
        }.build()
      }
      return DownloadListVersions.newBuilder().apply {
        addAllTrackedTopicInfo(topicInfos)
        addAllTrackedSkillInfo(skillInfos)
      }.build()
    }

    private fun DownloadableTopicSummaryDto.captureVersions(): TopicInfo {
      return TopicInfo.newBuilder().apply {
        this.id = this@captureVersions.id
        this.contentVersion = this@captureVersions.contentVersion
        addAllStoryInfo(this@captureVersions.storySummariesList.map { it.captureVersions() })
        addAllSubtopicInfo(this@captureVersions.subtopicSummariesList.map { it.captureVersion() })
      }.build()
    }

    private fun StorySummaryDto.captureVersions(): StoryInfo {
      return StoryInfo.newBuilder().apply {
        this.id = this@captureVersions.id
        this.contentVersion = this@captureVersions.contentVersion
        addAllChapterInfo(this@captureVersions.chaptersList.map { it.captureVersion() })
      }.build()
    }

    private fun ChapterSummaryDto.captureVersion(): ChapterInfo {
      return ChapterInfo.newBuilder().apply {
        this.explorationId = this@captureVersion.explorationId
        this.explorationContentVersion = this@captureVersion.contentVersion
      }.build()
    }

    private fun SubtopicSummaryDto.captureVersion(): SubtopicInfo {
      return SubtopicInfo.newBuilder().apply {
        this.index = this@captureVersion.index
        this.contentVersion = this@captureVersion.contentVersion
      }.build()
    }
  }
}

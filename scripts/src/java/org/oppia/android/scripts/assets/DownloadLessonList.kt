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
import org.oppia.android.scripts.proto.DownloadConfig
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
  check(args.size in 6..7) {
    "Expected use: bazel run //scripts:download_lesson_list <base_url> <gcs_base_url>" +
      " <gcs_bucket> </path/to/api/secret.file> </path/to/output_list.textproto>" +
      " </path/to/download_config.[textproto,pb]> [</path/to/api/debug/dir>]"
  }

  val baseUrl = args[0]
  val gcsBaseUrl = args[1]
  val gcsBucket = args[2]
  val apiSecretPath = args[3]
  val outputFilePath = args[4]
  val downloadConfigPath = args[5]
  val apiDebugPath = args.getOrNull(6)
  val apiSecretFile = File(apiSecretPath).absoluteFile.normalize().also {
    check(it.exists() && it.isFile) { "Expected API secret file to exist: $apiSecretPath." }
  }
  val outputFile = File(outputFilePath).absoluteFile.normalize()
  val downloadConfigFile = File(downloadConfigPath).absoluteFile.normalize().also {
    check(it.exists() && it.isFile) {
      "Expected config proto file to exist: $downloadConfigPath."
    }
  }
  val apiDebugDir = apiDebugPath?.let { path ->
    File(path).absoluteFile.normalize().also {
      check(if (!it.exists()) it.mkdirs() else it.isDirectory) {
        "Expected API debug directory to exist or to be creatable: $path."
      }
    }
  }

  val apiSecret = apiSecretFile.readText().trim()
  val downloadConfig = when (downloadConfigFile.extension) {
    "pb" -> downloadConfigFile.inputStream().buffered().use(DownloadConfig::parseFrom)
    "textproto" -> // TODO: Force pb to be used.
      TextFormat.parse(downloadConfigFile.readText(), DownloadConfig::class.java)
    else -> error("Invalid extension for config proto file: $downloadConfigPath.")
  }

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val downloader = LessonListDownloader(
      baseUrl, gcsBaseUrl, gcsBucket, apiSecret, apiDebugDir, scriptBgDispatcher, downloadConfig
    )
    runBlocking { downloader.downloadLessonListAsync(outputFile).await() }
  }
}

class LessonListDownloader(
  gaeBaseUrl: String,
  gcsBaseUrl: String,
  gcsBucket: String,
  apiSecret: String,
  private val apiDebugDir: File?,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val downloadConfig: DownloadConfig
) {
  private val gcsService by lazy { GcsService(gcsBaseUrl, gcsBucket) }
  private val imageDownloader by lazy { ImageDownloader(gcsService, scriptBgDispatcher) }
  private val androidEndpoint: GaeAndroidEndpoint by lazy {
    GaeAndroidEndpointJsonImpl(
      apiSecret,
      gaeBaseUrl,
      apiDebugDir,
      forceCacheLoad = false,
      downloadQuestions = false,
      scriptBgDispatcher,
      imageDownloader,
      forcedVersions = null, // Always load latest when creating the pin versions list.
      downloadConfig = downloadConfig,
      filterInvalidTopics = false // Do this during the main download instead.
    )
  }

  fun downloadLessonListAsync(lessonListOutputFile: File): Deferred<Unit> {
    return CoroutineScope(scriptBgDispatcher).async {
      if (apiDebugDir != null) {
        println("Config: Using ${apiDebugDir.path}/ for storing API responses (for debugging).")
      }

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
